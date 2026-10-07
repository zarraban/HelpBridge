package com.example.help_bridge.fundraising.fund.dto.request;

import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FundStatusUpdateRequest(
        @NotNull(message = "Status is required")
        FundStatus status,

        @NotNull(message = "Admin id is required")
        Long adminId,

        @Size(max = 1000)
        String comment
) {
}
