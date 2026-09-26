package com.example.help_bridge.request.controller;

import com.example.help_bridge.request.dto.request.RequestBookingDto;
import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.request.service.RequestBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestBookingController {

    private final RequestBookingService requestBookingService;

    public RequestBookingController(RequestBookingService requestBookingService) {
        this.requestBookingService = requestBookingService;
    }

    @PostMapping("/{id}/fundraiser")
    public ResponseEntity<RequestResponse> bookRequest(
            @PathVariable Long id,
            @RequestBody @Valid RequestBookingDto request) {

        RequestResponse updated = requestBookingService.bookRequest(id, request.fundId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updated);
    }
}