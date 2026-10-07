package com.example.help_bridge.fundraising.request.controller;

import com.example.help_bridge.fundraising.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.fundraising.request.service.RequestVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Request verification", description = "Верифікація запитів")
public class RequestVerificationController {

    private final RequestVerificationService verificationService;

    public RequestVerificationController(RequestVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Operation(summary = "Схвалити або відхилити запит",
            description = "approved=true переводить запит у схвалений стан. "
                    + "approved=false відхиляє запит і видаляє його.")
    @ApiResponse(responseCode = "204", description = "Рішення збережено")
    @ApiResponse(responseCode = "404", description = "Запит не знайдено",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Запит у некоректному стані",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping("/{id}/verification")
    public ResponseEntity<Void> verifyRequest(
            @PathVariable Long id,
            @RequestBody @Valid RequestVerificationDto reviewRequest) {

        verificationService.reviewRequest(id, reviewRequest);
        return ResponseEntity.noContent().build();
    }
}