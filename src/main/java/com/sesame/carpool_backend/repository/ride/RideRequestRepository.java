package com.sesame.carpool_backend.repository.ride;

import com.sesame.carpool_backend.model.ride.Ride;
import com.sesame.carpool_backend.model.ride.RideRequest;
import com.sesame.carpool_backend.model.ride.RideRequestStatus;
import com.sesame.carpool_backend.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RideRequestRepository extends JpaRepository<RideRequest, Long> {
    boolean existsByPassengerAndRide(User passenger, Ride ride);

    Page<RideRequest> findByPassengerAndStatus(User passenger, RideRequestStatus status, Pageable pageable);

    Page<RideRequest> findByPassenger(User passenger, Pageable pageable);

    Page<RideRequest> findByRide_DriverAndStatus(User driver, RideRequestStatus status, Pageable pageable);

    @Query("SELECT r.passenger FROM RideRequest r WHERE r.status = 'ACCEPTED' GROUP BY r.passenger ORDER BY COUNT(r) DESC")
    List<User> findPassengerWithMostRideRequests();
}
