package com.example.help_bridge.fund.event;

import com.example.help_bridge.fund.entity.FundStatus;

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
