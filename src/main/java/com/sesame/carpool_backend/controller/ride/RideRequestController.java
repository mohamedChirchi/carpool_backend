package com.sesame.carpool_backend.controller.ride;

import com.sesame.carpool_backend.model.ride.RideRequestStatus;
import com.sesame.carpool_backend.model.user.User;
import com.sesame.carpool_backend.payload.request.ApplyForRideRequest;
import com.sesame.carpool_backend.service.ride.RideRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/ride-requests")
@RequiredArgsConstructor
public class RideRequestController {
    private final RideRequestService rideRequestService;

    @PostMapping("/passenger/apply")
    public ResponseEntity<Object> applyForRide(@RequestBody ApplyForRideRequest request, Principal connectedUser) {
        return ResponseEntity.ok(rideRequestService.createRideRequest(request, connectedUser));
    }
    @PostMapping("/passenger/cancel")
    public ResponseEntity<Object> cancelRideRequest(@RequestParam long rideRequestId ,Principal connectedUser) {
        return ResponseEntity.ok(rideRequestService.cancelRideRequest(rideRequestId,connectedUser));
    }


    @GetMapping("/passenger/applied")
    public ResponseEntity<Object> getAppliedRides(
            Principal connectedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) RideRequestStatus status
    ) {
        User passenger = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        return ResponseEntity.ok(rideRequestService.getAppliedRides(passenger, status, page, size));
    }

    @GetMapping("/driver")
    public ResponseEntity<Object> getRequestedRidesForDriver(
            Principal connectedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(rideRequestService.getRequestedRidesForDriver(connectedUser, page, size));
    }

    @PostMapping("/driver/accept")
    public ResponseEntity<Object> acceptRideRequest(@RequestParam Long rideRequestId) {
        return ResponseEntity.ok(rideRequestService.acceptRideRequest(rideRequestId));
    }

    @PostMapping("/driver/decline")
    public ResponseEntity<Object> declineRideRequest(@RequestParam Long rideRequestId) {
        return ResponseEntity.ok(rideRequestService.declineRideRequest(rideRequestId));
    }
}
