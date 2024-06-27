package com.sesame.carpool_backend.service.ride;

import com.sesame.carpool_backend.exception.ResourceNotFoundException;
import com.sesame.carpool_backend.model.ride.Ride;
import com.sesame.carpool_backend.model.ride.RideRequestStatus;
import com.sesame.carpool_backend.model.ride.RideStatus;
import com.sesame.carpool_backend.model.user.Car;
import com.sesame.carpool_backend.model.user.User;
import com.sesame.carpool_backend.payload.request.AddRideRequest;
import com.sesame.carpool_backend.payload.request.FilterRideRequest;
import com.sesame.carpool_backend.payload.response.MessageResponse;
import com.sesame.carpool_backend.payload.response.RideDriverResponse;
import com.sesame.carpool_backend.payload.response.RideResponse;
import com.sesame.carpool_backend.repository.ride.RideRepository;
import com.sesame.carpool_backend.repository.user.CarRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideServiceImp implements RideService {
    private final RideRepository rideRepository;
    private final EntityManager entityManager;
    private final CarRepository carRepository;

    @Override
    public Object createRide(AddRideRequest request, Principal connectedUser) {
        User authUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        entityManager.detach(authUser);

        Car car = carRepository.findById(request.getCardId()).orElseThrow();

        Ride ride = Ride.builder()
                .departureLocation(request.getDepartureLocation())
                .destinationLocation(request.getDestinationLocation())
                .departureDate(request.getDepartureDate())
                .price(request.getPrice())
                .status(RideStatus.ACTIVE)
                .car(car)
                .driver(authUser)
                .build();

        rideRepository.save(ride);

        return MessageResponse.builder()
                .message("Ride created successfully")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public Object getRidesCreatedByAuthenticatedDriver(Principal connectedUser, int page, int size) {
        User authUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        //Pageable pageable = PageRequest.of(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Ride> rides = rideRepository.findByDriverId(authUser.getId(),pageable);

        List<RideDriverResponse.RideInfo> rideInfo = rides.stream()
                .map(ride -> {
                    int availableSeats = getAvailableSeats(ride.getId());

                    return new RideDriverResponse.RideInfo(
                            ride.getId(),
                            ride.getDepartureLocation(),
                            ride.getDestinationLocation(),
                            ride.getDepartureDate(),
                            ride.getStatus().toString(),
                            ride.getPrice(),
                            ride.getCar().getId(),
                            ride.getCar().getBrand(),
                            ride.getCar().getModel(),
                            ride.getCar().getColor(),
                            ride.getCar().getSeats(),
                            availableSeats
                    );
                })
                .collect(Collectors.toList());

        return RideDriverResponse.builder()
                .rides(rideInfo)
                .totalPages(rides.getTotalPages())
                .totalElements(rides.getTotalElements())
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public Object filterRides(FilterRideRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Ride> filteredRidesPage = rideRepository.findByFilters(
                request.getDeparture(),
                request.getDestination(),
                request.getStatus(),
                request.getMinPrice(),
                request.getMaxPrice(),
                pageable
        );

        List<RideResponse.RideInfo> rideInfo = filteredRidesPage.getContent().stream()
                .map(ride -> {
                    int availableSeats = getAvailableSeats(ride.getId());
                    User driver = ride.getDriver();
                    return new RideResponse.RideInfo(
                            ride.getId(),
                            ride.getDepartureLocation(),
                            ride.getDestinationLocation(),
                            ride.getDepartureDate(),
                            ride.getStatus().toString(),
                            ride.getPrice(),
                            ride.getCar().getId(),
                            ride.getCar().getBrand(),
                            ride.getCar().getModel(),
                            ride.getCar().getColor(),
                            ride.getCar().getSeats(),
                            availableSeats,
                            driver.getImageUrl() != null ? driver.getImageUrl() : "/uploads/img/default/default_profile.png"
                    );
                })
                .collect(Collectors.toList());

        return RideResponse.builder()
                .rides(rideInfo)
                .totalPages(filteredRidesPage.getTotalPages())
                .totalElements(filteredRidesPage.getTotalElements())
                .http_code(HttpStatus.OK.value())
                .build();
    }

    public Object getLatestRides(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Ride> latestRidesPage = rideRepository.findAllByOrderByDepartureDateDesc(pageable);

        List<RideResponse.RideInfo> rideInfo = latestRidesPage.getContent().stream()
                .map(ride -> {
                    int availableSeats = getAvailableSeats(ride.getId());
                    User driver = ride.getDriver();
                    return new RideResponse.RideInfo(
                            ride.getId(),
                            ride.getDepartureLocation(),
                            ride.getDestinationLocation(),
                            ride.getDepartureDate(),
                            ride.getStatus().toString(),
                            ride.getPrice(),
                            ride.getCar().getId(),
                            ride.getCar().getBrand(),
                            ride.getCar().getModel(),
                            ride.getCar().getColor(),
                            ride.getCar().getSeats(),
                            availableSeats,
                            driver.getImageUrl() != null ? driver.getImageUrl() : "/uploads/img/default/default_profile.png"
                    );
                })
                .collect(Collectors.toList());

        return RideResponse.builder()
                .rides(rideInfo)
                .totalPages(latestRidesPage.getTotalPages())
                .totalElements(latestRidesPage.getTotalElements())
                .http_code(HttpStatus.OK.value())
                .build();
    }

    public int getAvailableSeats(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId));
        int totalSeats = ride.getCar().getSeats();
        int acceptedSeats = (int) ride.getRideRequests().stream()
                .filter(request -> request.getStatus() == RideRequestStatus.ACCEPTED)
                .count();

        return totalSeats - acceptedSeats;
    }
}
