package com.sesame.carpool_backend.payload.response;

import lombok.*;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class CarResponse {
    private int http_code;
    private Set<CarInfo> driverCars;
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    @Builder
    public static class CarInfo {
        private Long id;
        private String carBrand;
        private String carModel;
        private String carColor;
        private int carSeats;
    }
}
