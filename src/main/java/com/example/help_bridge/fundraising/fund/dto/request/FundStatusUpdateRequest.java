package com.example.help_bridge.fundraising.fund.dto.request;

import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import jakarta.validation.constraints.NotNull;

public record FundStatusUpdateRequest(
        @NotNull(message = "Status is required")
        FundStatus status
) {
}
