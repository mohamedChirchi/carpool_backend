package com.sesame.carpool_backend.payload.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDriverResponse {
    private List<RideRequestDriverResponse.RideRequestInfo> ridesRequest;
    private int http_code;
    private int totalPages;
    private long totalElements;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class RideRequestInfo {
        private Long id;
        private String statusRideRequest;
        private LocalDateTime requestDate;

        private String statusRide;
        private LocalDateTime departureDate;
        private String departureLocation;
        private String destinationLocation;

        private String passengerFirstName;
        private String passengerLastName;
        private String passengerImageUrl;
    }
}
