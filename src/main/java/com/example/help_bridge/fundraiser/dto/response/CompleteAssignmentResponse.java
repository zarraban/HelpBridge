package com.example.help_bridge.fundraiser.dto.response;

import com.example.help_bridge.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraiser.entity.FundraiserStatus;

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
