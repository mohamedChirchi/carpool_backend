package com.sesame.carpool_backend.payload.request;

import com.sesame.carpool_backend.model.ride.RideStatus;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FilterRideRequest {
    private String departure;
    private String destination;
    private RideStatus status;
    private Double minPrice;
    private Double maxPrice;
}
