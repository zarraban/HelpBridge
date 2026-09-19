package com.example.help_bridge.request.dto;

import jakarta.validation.constraints.NotNull;

public record RequestBookingDto(
        @NotNull(message = "Fund ID is mandatory")
        Long fundId
) {}
