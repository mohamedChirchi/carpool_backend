package com.sesame.carpool_backend.service.user;

import com.sesame.carpool_backend.payload.request.ChangePasswordRequest;
import com.sesame.carpool_backend.payload.request.UpdateUserDataRequest;

import java.security.Principal;

public interface UserService {
    boolean seedInitialUsers();

    Object changePassword(ChangePasswordRequest email, Principal connectedUser);

    Object updateUserData(UpdateUserDataRequest email, Principal connectedUser);
}
