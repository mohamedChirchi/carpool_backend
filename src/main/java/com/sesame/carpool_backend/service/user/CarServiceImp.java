package com.sesame.carpool_backend.service.user;

import com.sesame.carpool_backend.model.user.Car;
import com.sesame.carpool_backend.model.user.User;
import com.sesame.carpool_backend.payload.request.AddCarRequest;
import com.sesame.carpool_backend.payload.response.CarResponse;
import com.sesame.carpool_backend.payload.response.MessageResponse;
import com.sesame.carpool_backend.repository.user.CarRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImp implements CarService{
    private final CarRepository carRepository;
    private final EntityManager entityManager;

    @Override
    public Object createCar(AddCarRequest request, Principal connectedUser) {
        User authUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        entityManager.detach(authUser);
        Car car = Car.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .color(request.getColor())
                .seats(request.getSeats())
                .user(authUser)
                .build();
        carRepository.save(car);

        return MessageResponse.builder()
                .message("Car created successfully")
                .http_code(HttpStatus.OK.value())
                .build();
    }

    @Override
    public Object getDriverCars(Principal connectedUser) {
        User authUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        entityManager.detach(authUser);
        Set<CarResponse.CarInfo> driverCars =
                carRepository.findByUser(authUser)
                .stream()
                        .filter(car->!car.isDeleted())
                .map(car -> CarResponse.CarInfo
                        .builder()
                        .carSeats(car.getSeats())
                        .id(car.getId())
                        .carModel(car.getModel())
                        .carColor(car.getColor())
                        .carBrand(car.getBrand())
                        .build())
                        .collect(Collectors.toSet());

        return CarResponse.builder()
                .http_code(HttpStatus.OK.value())
                .driverCars(driverCars)
                .build();
    }

    @Override
    public Object deleteCar(Long id, Principal connectedUser) {
        User authUser = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        entityManager.detach(authUser);
        Car car=carRepository.findById(id).orElseThrow();
        User carOwner=car.getUser();
        // we should check if this car is his car
        if (carOwner.equals(authUser)) {
            car.setDeleted(true);
            carRepository.save(car);
            return MessageResponse.builder()
                    .http_code(HttpStatus.OK.value())
                    .message("Car deleted successfully")
                    .build();
        } else {
            return MessageResponse.builder()
                    .http_code(HttpStatus.FORBIDDEN.value())
                    .message("Error")
                    .build();
        }
    }
}
