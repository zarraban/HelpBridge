package com.example.help_bridge.dto.response;

import com.example.help_bridge.entity.enums.FundraiserStatus;

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
