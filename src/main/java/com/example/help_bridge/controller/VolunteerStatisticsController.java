package com.example.help_bridge.controller;

import com.example.help_bridge.dto.VolunteerStatisticsRequest;
import com.example.help_bridge.dto.VolunteerStatisticsResponse;
import com.example.help_bridge.service.VolunteerStatisticsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/volunteers/{volunteerId}/statistics")
public class VolunteerStatisticsController {

    private final VolunteerStatisticsService service;

    public VolunteerStatisticsController(VolunteerStatisticsService service) {
        this.service = service;
    }

    //GET /api/volunteers/{volunteerId}/statistics?from=2026-01-01&to=2026-09-14
    @GetMapping
    public ResponseEntity<VolunteerStatisticsResponse> getClosedFundraisersStatistics(
            @PathVariable UUID volunteerId,
            @Valid @ModelAttribute VolunteerStatisticsRequest period
    ) {
        return ResponseEntity.ok(service.getClosedFundraisersStatistics(volunteerId, period));
    }
}
