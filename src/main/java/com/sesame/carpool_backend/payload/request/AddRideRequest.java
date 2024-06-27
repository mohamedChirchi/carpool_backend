package com.sesame.carpool_backend.payload.request;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddRideRequest {
    private String departureLocation;
    private String destinationLocation;
    private LocalDateTime departureDate;
    private Double price;
    private Long cardId;
}
