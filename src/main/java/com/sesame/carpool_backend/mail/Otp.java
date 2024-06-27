package com.sesame.carpool_backend.mail;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Otp {
    public String generateOtp() {
        Random random = new Random();
        int randomNumber = random.nextInt(999999);
        String output = Integer.toString(randomNumber);

        while (output.length() < 6) {
            output = "0" + output;
        }
        return output;
    }
}
