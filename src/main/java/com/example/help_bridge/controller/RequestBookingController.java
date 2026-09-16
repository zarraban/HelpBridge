package com.example.help_bridge.controller;

import com.example.help_bridge.dto.RequestBookingDto.BookRequestRequest;
import com.example.help_bridge.service.RequestBookingService;
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
            @RequestBody @Valid BookRequestRequest request) {

        requestBookingService.bookRequest(id, request.fundId());
        return ResponseEntity.noContent().build();
    }
}