package com.example.help_bridge.fundraising.fundraiser.dto.response;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;

import java.time.LocalDateTime;

public record CompleteAssignmentResponse(
        Long assignmentId,
        Long fundraiserId,
        AssignmentStatus status,
        LocalDateTime finishedAt,
        FundraiserStatus fundraiserStatus,
        int evidenceCount
) {
}
