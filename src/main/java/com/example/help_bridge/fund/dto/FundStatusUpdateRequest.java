package com.example.help_bridge.fund.dto;

import com.example.help_bridge.fund.entity.FundStatus;
import jakarta.validation.constraints.NotNull;

public record FundStatusUpdateRequest(
        @NotNull(message = "Status is required")
        FundStatus status
) {
}
