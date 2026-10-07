package com.example.help_bridge.fundraising.request.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Бронювання запиту фондом")
public record RequestBookingDto(
        @Schema(description = "Id фонду, який бронює запит", example = "1")
        @NotNull(message = "Fund ID is mandatory")
        Long fundId
) {}