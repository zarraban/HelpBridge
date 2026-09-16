package com.example.help_bridge.dto;

import com.example.help_bridge.entity.FundStatus;
import jakarta.validation.constraints.NotNull;

public record FundStatusUpdateRequest(
        @NotNull(message = "Status is required")
        FundStatus status
) {
}
