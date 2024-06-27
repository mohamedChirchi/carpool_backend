package com.sesame.carpool_backend.controller.user;

import com.sesame.carpool_backend.payload.request.ChangePasswordRequest;
import com.sesame.carpool_backend.payload.request.UpdateUserDataRequest;
import com.sesame.carpool_backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping("/seed")
    public ResponseEntity<Boolean> seedUsers() {
        return ResponseEntity.ok().body(service.seedInitialUsers());
    }

    @GetMapping("/protected")
    public ResponseEntity<String> needsToken() {
        return ResponseEntity.ok().body("entered a protected route with a bearer token");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> onlyAdmin() {
        return ResponseEntity.ok().body("protected route and for admin only");
    }

    @PatchMapping("/update/password")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal connectedUser) {
        return ResponseEntity.ok().body(service.changePassword(request, connectedUser));
    }

    @PatchMapping("/update/data")
    public ResponseEntity<Object> updateUserData(
            @Valid @ModelAttribute UpdateUserDataRequest request,
            Principal connectedUser
    ) {
        return ResponseEntity.ok().body(service.updateUserData(request, connectedUser));
    }
}
