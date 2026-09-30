package com.example.help_bridge.fundraising.request.dto.request;

import jakarta.validation.constraints.NotNull;

public record RequestBookingDto(
        @NotNull(message = "Fund ID is mandatory")
        Long fundId
) {}
