package com.sesame.carpool_backend.service.auth;

import com.sesame.carpool_backend.config.jwt.JwtService;
import com.sesame.carpool_backend.exception.ResourceNotFoundException;
import com.sesame.carpool_backend.mail.EmailSender;
import com.sesame.carpool_backend.mail.Otp;
import com.sesame.carpool_backend.model.user.ResetPassword;
import com.sesame.carpool_backend.model.user.Token;
import com.sesame.carpool_backend.model.user.TokenType;
import com.sesame.carpool_backend.model.user.Role;
import com.sesame.carpool_backend.model.user.User;
import com.sesame.carpool_backend.payload.request.*;
import com.sesame.carpool_backend.payload.response.ErrorResponse;
import com.sesame.carpool_backend.payload.response.LoginResponse;
import com.sesame.carpool_backend.payload.response.MessageResponse;
import com.sesame.carpool_backend.repository.user.ResetPasswordRepository;
import com.sesame.carpool_backend.repository.user.TokenRepository;
import com.sesame.carpool_backend.repository.user.RoleRepository;
import com.sesame.carpool_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImp implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final Otp otpCmp;
    private final EmailSender emailSenderCmp;
    private final ResetPasswordRepository resetPasswordRepository;
    private final RoleRepository roleRepository;
    @Value("${carpool_app.frontend.url}")
    private String frontUrl;

    public Object register(RegisterRequest request) {

        Set<Role> roles = request.getRoles().stream()
                .map(
                        roleName -> roleRepository.findByName(roleName).orElseThrow(
                                () -> new ResourceNotFoundException("Role not found for name: " + roleName)
                        )
                )
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            return ErrorResponse.builder()
                    .errors(List.of("Invalid roles provided"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }

        String otpCode = otpCmp.generateOtp();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .roles(roles)
                .otp(otpCode)
                .otpGeneratedTime(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        emailSenderCmp.sendOtpVerification(savedUser.getEmail(), otpCode);
        return MessageResponse.builder()
                .message("Registration done, check your email to verify your account with the OTP code")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    public Object login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            User user = userRepository.findByEmailWithRoles(request.getEmail()).orElseThrow(
                    () -> new ResourceNotFoundException("User not found for email: " + request.getEmail())
            );
            if (user.isVerified()) {
                String jwtToken = jwtService.generateToken(user);

                revokeAllUserTokens(user);
                saveUserToken(user, jwtToken);

                return LoginResponse.builder()
                        .token(jwtToken)
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .address(user.getAddress())
                        .phoneNumber(user.getPhoneNumber())
                        .imgUrl(user.getImageUrl() != null ? user.getImageUrl() : "/uploads/img/default/default_profile.png")
                        .message("Welcome to sesame-Carpool project")
                        .http_code(HttpStatus.OK.value())
                        .build();
            }
            return ErrorResponse.builder()
                    .errors(List.of("Your account is not verified"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        } catch (BadCredentialsException e) {
            return ErrorResponse.builder()
                    .errors(List.of("Invalid email or password. Please try again"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public Object verifyAccount(VerifyAccountRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found for email: " + request.getEmail())
                );
        LocalDateTime otpGeneratedTime = user.getOtpGeneratedTime();
        LocalDateTime currentTime = LocalDateTime.now();
        long secondsDifference = Duration.between(otpGeneratedTime, currentTime).getSeconds();
        boolean isNotExpired = secondsDifference < 60;

        if (user.getOtp().equals(request.getOtp())) {
            if (isNotExpired) {
                if (!user.isVerified()) {
                    user.setVerified(true);
                    userRepository.save(user);
                    return MessageResponse.builder()
                            .message("Your OTP has been successfully verified. You now have access to the platform")
                            .http_code(HttpStatus.OK.value())
                            .build();
                } else {
                    return ErrorResponse.builder()
                            .errors(List.of("Your account is already verified. You have access to the platform"))
                            .http_code(HttpStatus.UNAUTHORIZED.value())
                            .build();
                }
            } else {
                return ErrorResponse.builder()
                        .errors(List.of("OTP Code expired. Please regenerate another OTP code"))
                        .http_code(HttpStatus.UNAUTHORIZED.value())
                        .build();
            }
        } else {
            return ErrorResponse.builder()
                    .errors(List.of("Invalid OTP code"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }
    }

    public Object regenerateOtp(RegenerateOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("User not found for email: " + request.getEmail())
        );
        if (!user.isVerified()) {
            String otpCode = otpCmp.generateOtp();
            user.setOtp(otpCode);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);

            emailSenderCmp.sendOtpVerification(request.getEmail(), otpCode);

            return MessageResponse.builder()
                    .message("A new OTP code has been generated and sent to your email")
                    .http_code(HttpStatus.OK.value())
                    .build();
        } else {
            return ErrorResponse.builder()
                    .errors(List.of("Your account is already verified. You have access to the platform"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("User not found for email: " + request.getEmail())
        );
        String url = generateResetToken(user);
        emailSenderCmp.sendResetPassword(request.getEmail(), url);
        return MessageResponse.builder()
                .message("Password reset instructions have been sent to your email")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    //Token reset password is set to be valid for 30mns
    public String generateResetToken(User user) {
        UUID uuid = UUID.randomUUID();
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime expirationDateTime = currentDateTime.plusMinutes(30);
        ResetPassword resetToken = ResetPassword.builder()
                .token(uuid.toString())
                .expirationDate(expirationDateTime)
                .user(user)
                .build();

        ResetPassword token = resetPasswordRepository.save(resetToken);
        return frontUrl + "/reset-password/" + token.getToken();
    }

    public Object resetPassword(String token, ResetPasswordRequest request) {
        ResetPassword resetPassword = resetPasswordRepository.findByToken(token).orElseThrow(
                () -> new ResourceNotFoundException("Token not found for token: " + token)
        );
        if (isResetPasswordTokenValid(resetPassword.getExpirationDate())) {
            User user = resetPassword.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return MessageResponse.builder()
                    .message("Password has been changed")
                    .http_code(HttpStatus.OK.value())
                    .build();
        } else {
            return ErrorResponse.builder()
                    .errors(List.of("Something went wrong"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }
    }

    public boolean isResetPasswordTokenValid(LocalDateTime expirationDate) {
        LocalDateTime currentDate = LocalDateTime.now();
        return expirationDate.isAfter(currentDate);
    }
}
