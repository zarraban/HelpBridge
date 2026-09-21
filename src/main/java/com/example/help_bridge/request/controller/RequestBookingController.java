package com.example.help_bridge.request.controller;

import com.example.help_bridge.request.dto.request.RequestBookingDto;
import com.example.help_bridge.request.service.RequestBookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fund/requests")
public class RequestBookingController {

    private final RequestBookingService requestBookingService;

    public RequestBookingController(RequestBookingService requestBookingService) {
        this.requestBookingService = requestBookingService;
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<Void> bookRequest(
            @PathVariable Long id,
            @RequestBody @Valid RequestBookingDto request) {

        requestBookingService.bookRequest(id, request.fundId());
        return ResponseEntity.noContent().build();
    }
}