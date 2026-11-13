package com.example.help_bridge.fundraising.fundraiser.dto.response;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;

import java.time.LocalDateTime;

public record FundraiserAssignmentResponse(
        Long assignmentId,
        Long volunteerId,
        AssignmentStatus status,
        LocalDateTime assignedAt,
        LocalDateTime finishedAt,
        String returnReason,
        Long fundraiserId,
        String fundraiserTitle,
        FundraiserStatus fundraiserStatus,
        String closingComment
) {
}
