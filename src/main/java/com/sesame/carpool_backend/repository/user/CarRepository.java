package com.sesame.carpool_backend.repository.user;

import com.sesame.carpool_backend.model.user.Car;
import com.sesame.carpool_backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface CarRepository extends JpaRepository<Car, Long> {
    Set<Car> findByUser(User user);
}
