package com.sesame.carpool_backend.controller.ride;

import com.sesame.carpool_backend.model.ride.Ride;
import com.sesame.carpool_backend.model.ride.RideStatus;
import com.sesame.carpool_backend.model.user.Car;
import com.sesame.carpool_backend.payload.request.AddRideRequest;
import com.sesame.carpool_backend.payload.request.FilterRideRequest;
import com.sesame.carpool_backend.payload.response.MessageResponse;
import com.sesame.carpool_backend.repository.ride.RideRepository;
import com.sesame.carpool_backend.repository.user.CarRepository;
import com.sesame.carpool_backend.service.ride.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;

    // start dummy

    private final CarRepository carRepository;
    private final RideRepository rideRepository;

    private String getRandomCity() {
        String[] cities = {"Ariana", "Tunis", "Sousse", "Sfax", "Bizerte"};
        int randomIndex = new Random().nextInt(cities.length);
        return cities[randomIndex];
    }
    private RideStatus getRandomRideStatus() {
        RideStatus[] statuses = RideStatus.values();
        int randomIndex = new Random().nextInt(statuses.length);
        return statuses[randomIndex];
    }
    @PostMapping("/generate")
    public Object generateRides() {
        Car car = carRepository.findById(1L).orElseThrow();
        for (int i = 0; i < 30; i++) {

            Ride ride = Ride.builder()
                    .departureLocation(getRandomCity())
                    .destinationLocation(getRandomCity())
                    .departureDate(LocalDateTime.now().plusHours(i))
                    .price(i + 0.0)
                    .status(getRandomRideStatus())
                    .car(car)
                    .driver(car.getUser())
                    .build();

            rideRepository.save(ride);
        }

        return MessageResponse.builder()
                .message("30 rides generated successfully")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    //end dummy
    @PostMapping("/driver")
    public ResponseEntity<Object> createRide(@RequestBody AddRideRequest request, Principal connectedUser) {
        return ResponseEntity.ok(rideService.createRide(request, connectedUser));
    }

    @GetMapping("/driver")
    public ResponseEntity<Object> getRidesCreatedByAuthenticatedDriver(
            Principal connectedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(rideService.getRidesCreatedByAuthenticatedDriver(connectedUser,page,size));
    }

    @PostMapping("/filter")
    public ResponseEntity<Object> filterRides(
            @RequestBody FilterRideRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(rideService.filterRides(request,page,size));
    }
    @GetMapping("/latest")
    public ResponseEntity<Object> getLatestRides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(rideService.getLatestRides(page, size));
    }
}