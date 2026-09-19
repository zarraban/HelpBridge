package com.example.help_bridge.fundraiser.dto.response;

import com.example.help_bridge.fundraiser.entity.AssignmentStatus;

import java.time.LocalDateTime;

public record FundraiserAssignmentResponse(
        Long assignmentId,
        Long volunteerId,
        AssignmentStatus status,
        LocalDateTime assignedAt,
        LocalDateTime finishedAt,
        String returnReason
) {
}
