package com.sesame.carpool_backend.service.auth;


import com.sesame.carpool_backend.payload.request.*;

public interface AuthenticationService {
    Object register(RegisterRequest request);

    Object login(LoginRequest request);

    Object verifyAccount(VerifyAccountRequest request);

    Object regenerateOtp(RegenerateOtpRequest request);

    Object forgotPassword(ForgotPasswordRequest request);

    Object resetPassword(String token, ResetPasswordRequest request);
}
