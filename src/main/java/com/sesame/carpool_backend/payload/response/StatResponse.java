package com.sesame.carpool_backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class StatResponse implements Serializable {
    Long totalPassengers;
    Long totalDrivers;
    Map<Object, Long> totalRidesPerYear;
    Map<Object, Long> totalRidesPerDay;
    String passengerWithMostRidesFullName;
    String passengerWithMostRidesPhoneNumber;
    String driverWithMostRidesFullName;
    String driverWithMostRidesPhoneNumber;

}
