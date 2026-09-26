package com.example.help_bridge.request.controller;

import com.example.help_bridge.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.request.service.RequestVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestVerificationController {

    private final RequestVerificationService verificationService;

    public RequestVerificationController(RequestVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PatchMapping("/{id}/verification")
    public ResponseEntity<Void> verifyRequest(
            @PathVariable Long id,
            @RequestBody @Valid RequestVerificationDto reviewRequest) {

        verificationService.reviewRequest(id, reviewRequest);
        return ResponseEntity.noContent().build();
    }
}