package com.example.help_bridge.fundraising.fundraiser.dto.response;

import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FundraiserResponse (
        Long fundraiserId,
        Long requestId,
        FundraiserStatus status,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        List<EvidenceResponse> evidences
){
}
