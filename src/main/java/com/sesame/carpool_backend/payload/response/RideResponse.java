package com.sesame.carpool_backend.payload.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RideResponse {
    private List<RideInfo> rides;
    private int http_code;
    private int totalPages;
    private long totalElements;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class RideInfo {
        private Long id;
        private String departureLocation;
        private String destinationLocation;
        private LocalDateTime departureDate;
        private String status;
        private Double price;
        private Long carId;
        private String carBrand;
        private String carModel;
        private String carColor;
        private int carSeats;
        private int carAvailableSeats;
        private String driverImageUrl;
    }
}
