package com.sesame.carpool_backend.service.user;

import com.sesame.carpool_backend.exception.ResourceNotFoundException;
import com.sesame.carpool_backend.model.user.Role;
import com.sesame.carpool_backend.model.user.User;
import com.sesame.carpool_backend.payload.request.ChangePasswordRequest;
import com.sesame.carpool_backend.payload.request.UpdateUserDataRequest;
import com.sesame.carpool_backend.payload.response.ErrorResponse;
import com.sesame.carpool_backend.payload.response.MessageResponse;
import com.sesame.carpool_backend.payload.response.UpdateDataResponse;
import com.sesame.carpool_backend.repository.user.RoleRepository;
import com.sesame.carpool_backend.repository.user.UserRepository;
import com.sesame.carpool_backend.service.utils.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final FileStorageService fileStorageService;

    private void seedRoles() {
        roleRepository.save(new Role(1L, "ADMIN"));
        roleRepository.save(new Role(2L, "DRIVER"));
        roleRepository.save(new Role(3L, "PASSENGER"));
    }

    public boolean seedInitialUsers() {
        seedRoles();
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow(
                () -> new ResourceNotFoundException("Role not found for name: ADMIN")
        );
        Role driverRole = roleRepository.findByName("DRIVER").orElseThrow(
                () -> new ResourceNotFoundException("Role not found for name: DRIVER")
        );
        Role passengerRole = roleRepository.findByName("PASSENGER").orElseThrow(
                () -> new ResourceNotFoundException("Role not found for name: PASSENGER")
        );

        User user1 = new User(1L, "User1", "Admin", "user1@gmail.com", BCrypt.hashpw("user1password", BCrypt.gensalt()), "Rades", "123456789", Collections.singleton(adminRole), true);
        User user2 = new User(2L, "User2", "DriverOnly", "user2@gmail.com", BCrypt.hashpw("user2password", BCrypt.gensalt()), "Tunis", "123456789", Collections.singleton(driverRole), true);
        User user3 = new User(3L, "User3", "DriverPassenger", "user3@gmail.com", BCrypt.hashpw("user3password", BCrypt.gensalt()), "Ariana", "123456789", new HashSet<>(Arrays.asList(driverRole, passengerRole)), true);

        User user4 = new User(4L, "User4", "DriverPassenger", "user4@gmail.com", BCrypt.hashpw("user4password", BCrypt.gensalt()), "Rades", "123456789", new HashSet<>(Arrays.asList(driverRole, passengerRole)), true);
        User user5 = new User(5L, "User5", "DriverPassenger", "user5@gmail.com", BCrypt.hashpw("user5password", BCrypt.gensalt()), "Tunis", "123456789", new HashSet<>(Arrays.asList(driverRole, passengerRole)), true);
        User user6 = new User(6L, "User6", "PassengerOnly", "user6@gmail.com", BCrypt.hashpw("user6password", BCrypt.gensalt()), "Ariana", "123456789", Collections.singleton(passengerRole), true);

        List<User> savedUsers = userRepository.saveAll(List.of(user1, user2, user3, user4, user5, user6));
        return !savedUsers.isEmpty();
    }

    @Override
    public Object changePassword(ChangePasswordRequest request, Principal connectedUser) {
        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ErrorResponse.builder()
                    .errors(List.of("Current password is wrong"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return MessageResponse.builder()
                .message("Password updated successfully")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public Object updateUserData(UpdateUserDataRequest request, Principal connectedUser) {

        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getImgUrl() != null && !request.getImgUrl().isEmpty()) {
            try {
                String fileName = fileStorageService.storeImg(request.getImgUrl());
                user.setImageUrl("/uploads/img/user/" + fileName);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        userRepository.save(user);

        return UpdateDataResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .imgUrl(user.getImageUrl())
                .message("User data updated successfully")
                .http_code(HttpStatus.OK.value())
                .build();
    }
}
