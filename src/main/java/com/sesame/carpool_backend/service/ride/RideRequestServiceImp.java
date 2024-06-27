package com.sesame.carpool_backend.service.ride;

import com.sesame.carpool_backend.exception.ResourceNotFoundException;
import com.sesame.carpool_backend.model.ride.Ride;
import com.sesame.carpool_backend.model.ride.RideRequest;
import com.sesame.carpool_backend.model.ride.RideRequestStatus;
import com.sesame.carpool_backend.model.user.User;
import com.sesame.carpool_backend.payload.request.ApplyForRideRequest;
import com.sesame.carpool_backend.payload.response.ErrorResponse;
import com.sesame.carpool_backend.payload.response.MessageResponse;
import com.sesame.carpool_backend.payload.response.RideRequestDriverResponse;
import com.sesame.carpool_backend.payload.response.RideRequestResponse;
import com.sesame.carpool_backend.repository.ride.RideRepository;
import com.sesame.carpool_backend.repository.ride.RideRequestRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideRequestServiceImp implements RideRequestService {
    private final RideRequestRepository rideRequestRepository;
    private final EntityManager entityManager;
    private final RideRepository rideRepository;
    @Override
    public Object cancelRideRequest(long rideRequestId, Principal connectedUser) {
        //check if this ride request is from the auth user
        if (rideRequestRepository.findById(rideRequestId).isEmpty()) {
            return ErrorResponse.builder()
                    .errors(List.of("Can't find this ride request ."))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }
        rideRequestRepository.deleteById(rideRequestId);
        return MessageResponse.builder()
                .message("Ride request canceled")
                .http_code(HttpStatus.OK.value())
                .build();    }

    @Override
    public Object createRideRequest(ApplyForRideRequest request, Principal connectedUser) {

        User passenger = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        entityManager.detach(passenger);


        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + request.getRideId()));

        if (rideRequestRepository.existsByPassengerAndRide(passenger, ride)) {
            return ErrorResponse.builder()
                    .errors(List.of("You have already requested this ride"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }

        if (ride.getDriver().getId().equals(passenger.getId())) {
            return ErrorResponse.builder()
                    .errors(List.of("You cannot apply for your own ride"))
                    .http_code(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }

        RideRequest rideRequest = RideRequest.builder()
                .passenger(passenger)
                .ride(ride)
                .requestDate(LocalDateTime.now())
                .status(RideRequestStatus.PENDING)
                .build();

        rideRequestRepository.save(rideRequest);

        return MessageResponse.builder()
                .message("Ride request created successfully")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public RideRequestResponse getAppliedRides(User passenger, RideRequestStatus status, int page, int size) {
        Page<RideRequest> rideRequests;
        //Pageable pageable = PageRequest.of(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());

        if (status != null) {
            rideRequests = rideRequestRepository.findByPassengerAndStatus(passenger, status, pageable);
        } else {
            rideRequests = rideRequestRepository.findByPassenger(passenger, pageable);
        }

        List<RideRequestResponse.RideRequestInfo> rideRequestInfo = rideRequests.stream()
                .map(rideRequest -> {
                    Ride ride = rideRequest.getRide();
                    return new RideRequestResponse.RideRequestInfo(
                            rideRequest.getId(),
                            rideRequest.getStatus().toString(),
                            rideRequest.getRequestDate(),

                            ride.getStatus().toString(),
                            ride.getDepartureDate(),
                            ride.getDepartureLocation(),
                            ride.getDestinationLocation()
                    );
                })
                .collect(Collectors.toList());

        return RideRequestResponse.builder()
                .ridesRequest(rideRequestInfo)
                .totalPages(rideRequests.getTotalPages())
                .totalElements(rideRequests.getTotalElements())
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public Object getRequestedRidesForDriver(Principal connectedUser, int page, int size) {
        //Pageable pageable = PageRequest.of(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());

        User driver = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        Page<RideRequest> requestedRides = rideRequestRepository.findByRide_DriverAndStatus(driver, RideRequestStatus.PENDING, pageable);

        List<RideRequestDriverResponse.RideRequestInfo> rideRequestInfo = requestedRides.stream()
                .map(rideRequest -> {
                    Ride ride = rideRequest.getRide();
                    User passenger = rideRequest.getPassenger();
                    return new RideRequestDriverResponse.RideRequestInfo(
                            rideRequest.getId(),
                            rideRequest.getStatus().toString(),
                            rideRequest.getRequestDate(),

                            ride.getStatus().toString(),
                            ride.getDepartureDate(),
                            ride.getDepartureLocation(),
                            ride.getDestinationLocation(),

                            passenger.getFirstName(),
                            passenger.getLastName(),
                            passenger.getImageUrl() != null ? passenger.getImageUrl() : "/uploads/img/default/default_profile.png"
                    );
                })
                .collect(Collectors.toList());

        return RideRequestDriverResponse.builder()
                .ridesRequest(rideRequestInfo)
                .totalPages(requestedRides.getTotalPages())
                .totalElements(requestedRides.getTotalElements())
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public Object acceptRideRequest(Long rideRequestId) {
        RideRequest rideRequest = rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideRequestId));

        rideRequest.setStatus(RideRequestStatus.ACCEPTED);
        rideRequestRepository.save(rideRequest);

        return MessageResponse.builder()
                .message("Ride request accepted successfully.")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public Object declineRideRequest(Long rideRequestId) {
        RideRequest rideRequest = rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideRequestId));

        rideRequest.setStatus(RideRequestStatus.DECLINED);
        rideRequestRepository.save(rideRequest);

        return MessageResponse.builder()
                .message("Ride request declined.")
                .http_code(HttpStatus.OK.value())
                .build();
    }
}
