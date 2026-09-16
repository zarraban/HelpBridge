package com.example.help_bridge.controller;

import com.example.help_bridge.dto.RequestVerificationDto;
import com.example.help_bridge.service.RequestVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/requests")
public class RequestVerificationController {

    private final RequestVerificationService verificationService;

    public RequestVerificationController(RequestVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<Void> verifyRequest(
            @PathVariable Long id,
            @RequestBody @Valid RequestVerificationDto reviewRequest) {

        verificationService.reviewRequest(id, reviewRequest);
        return ResponseEntity.noContent().build();
    }
}