package com.sesame.carpool_backend.service.admin;

import com.sesame.carpool_backend.model.ride.Ride;
import com.sesame.carpool_backend.model.user.User;
import com.sesame.carpool_backend.payload.response.StatResponse;
import com.sesame.carpool_backend.repository.ride.RideRepository;
import com.sesame.carpool_backend.repository.ride.RideRequestRepository;
import com.sesame.carpool_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpli implements StatisticsService {
    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final RideRequestRepository rideRequestRepository;

    @Override
    public Long getTotalPassenger() {
        return userRepository.countUsersByRoles_Name("PASSENGER");
    }

    @Override
    public Long getTotalDrivers() {
        return userRepository.countUsersByRoles_Name("DRIVER");
    }

    @Override
    public Map<Object, Long> getTotalRidesPerYear() {
        List<Ride> rides = rideRepository.findAll();
        return rides.stream()
                .collect(Collectors.groupingBy(
                        ride -> ride.getDepartureDate().getYear(),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<Object, Long> getTotalRidesPerDay() {
        List<Ride> rides = rideRepository.findAll();

        return rides.stream()
                .collect(Collectors.groupingBy(
                        ride -> ride.getDepartureDate().toLocalDate(),
                        Collectors.counting()
                ));
    }

    @Override
    public StatResponse getAllStatistics() {
        var passenger = getPassengerWithMostRides();
        var driver = getDriverWithMostRides();

        return StatResponse.builder()
                .totalPassengers(getTotalPassenger())
                .totalDrivers(getTotalDrivers())
                .totalRidesPerDay(getTotalRidesPerDay())
                .passengerWithMostRidesFullName(passenger.getFirstName() + " " + passenger.getLastName())
                .passengerWithMostRidesPhoneNumber(passenger.getPhoneNumber())
                .driverWithMostRidesFullName(driver.getFirstName() + " " + driver.getLastName())
                .driverWithMostRidesPhoneNumber(driver.getPhoneNumber())
                .totalRidesPerYear(getTotalRidesPerYear())
                .build();
    }

    @Override
    public User getDriverWithMostRides() {
        List<User> drivers = userRepository.findAll();

        return drivers.stream()
                .max(Comparator.comparingLong(u -> u.getRides().size()))
                .orElse(null);

    }

    /*
        @Override
        public User getPassengerWithMostRides() {
            List<User> passengers = userRepository.findAll();
            return passengers.stream()
                    .filter(user -> user.getRides() != null)
                    .max(Comparator.comparingInt(user -> user.getRides().size()))
                    .orElse(null);
        }

     */
    public User getPassengerWithMostRides() {
        List<User> passengers = rideRequestRepository.findPassengerWithMostRideRequests();
        return passengers.isEmpty() ? null : passengers.get(0);
    }
}

