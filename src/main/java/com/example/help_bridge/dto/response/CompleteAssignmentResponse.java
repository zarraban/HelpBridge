package com.example.help_bridge.dto.response;

import com.example.help_bridge.entity.enums.AssignmentStatus;
import com.example.help_bridge.entity.enums.FundraiserStatus;

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
