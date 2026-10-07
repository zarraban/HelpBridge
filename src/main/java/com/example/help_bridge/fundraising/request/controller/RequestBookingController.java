package com.example.help_bridge.fundraising.request.controller;

import com.example.help_bridge.fundraising.request.dto.request.RequestBookingDto;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.service.RequestBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Request booking", description = "Бронювання запитів фондами")
public class RequestBookingController {

    private final RequestBookingService requestBookingService;

    public RequestBookingController(RequestBookingService requestBookingService) {
        this.requestBookingService = requestBookingService;
    }

    @Operation(summary = "Забронювати запит фондом",
            description = "Фонд має бути схвалений, а запит верифікований.")
    @ApiResponse(responseCode = "202", description = "Запит заброньовано")
    @ApiResponse(responseCode = "404", description = "Запит або фонд не знайдені",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Фонд не схвалений або запит у некоректному стані",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{id}/fundraiser")
    public ResponseEntity<RequestResponse> bookRequest(
            @PathVariable Long id,
            @RequestBody @Valid RequestBookingDto request) {

        RequestResponse updated = requestBookingService.bookRequest(id, request.fundId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updated);
    }
}