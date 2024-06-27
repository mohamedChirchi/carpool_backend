package com.sesame.carpool_backend.service.ride;

import com.sesame.carpool_backend.payload.request.AddRideRequest;
import com.sesame.carpool_backend.payload.request.FilterRideRequest;

import java.security.Principal;

public interface RideService {
    Object createRide(AddRideRequest request, Principal connectedUser);
    Object getRidesCreatedByAuthenticatedDriver(Principal connectedUser, int page, int size);
    Object filterRides(FilterRideRequest request, int page, int size);
    Object getLatestRides(int page, int size);
}
