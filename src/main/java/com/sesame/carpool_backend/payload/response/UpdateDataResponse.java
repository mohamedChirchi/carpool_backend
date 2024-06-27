package com.sesame.carpool_backend.payload.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDataResponse {
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String imgUrl;
    private String message;
    private int http_code;
}