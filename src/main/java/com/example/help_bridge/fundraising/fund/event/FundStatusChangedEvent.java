package com.example.help_bridge.fundraising.fund.event;

import com.example.help_bridge.fundraising.fund.entity.FundStatus;

import java.time.Instant;

public record FundStatusChangedEvent(
        Long fundId,
        FundStatus previousStatus,
        FundStatus newStatus,
        Instant occurredAt
) {
    public FundStatusChangedEvent(Long fundId, FundStatus previousStatus, FundStatus newStatus){
        this(fundId, previousStatus, newStatus, Instant.now());
    }
}
