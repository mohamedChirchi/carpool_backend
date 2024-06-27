package com.sesame.carpool_backend.payload.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddCarRequest {
    private String brand;
    private String model;
    private String color;
    private int seats;
}
