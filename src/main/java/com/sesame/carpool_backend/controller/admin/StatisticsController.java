package com.sesame.carpool_backend.controller.admin;

import com.sesame.carpool_backend.service.admin.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    @GetMapping
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok().body(statisticsService.getAllStatistics());
    }
}
