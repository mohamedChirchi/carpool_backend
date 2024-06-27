package com.sesame.carpool_backend.payload.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String imgUrl;
    private String token;
    private String message;
    private int http_code;
}
