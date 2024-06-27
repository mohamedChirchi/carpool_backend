package com.sesame.carpool_backend.controller.user;

import com.sesame.carpool_backend.payload.request.AddCarRequest;
import com.sesame.carpool_backend.service.user.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;
    @PostMapping("/")
    public ResponseEntity<Object> createCar(@RequestBody AddCarRequest request, Principal connectedUser) {
        return ResponseEntity.ok().body(carService.createCar(request,connectedUser));
    }
    @GetMapping("/get-cars")
    public ResponseEntity<Object> getDriverCars(Principal connectedUser) {
        return ResponseEntity.ok().body(carService.getDriverCars(connectedUser));
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteCar(@RequestParam Long id, Principal connectedUser) {
        return ResponseEntity.ok().body(carService.deleteCar(id,connectedUser));
    }


}
