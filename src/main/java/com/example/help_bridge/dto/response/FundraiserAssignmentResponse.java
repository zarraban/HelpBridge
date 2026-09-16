package com.example.help_bridge.dto.response;

import com.example.help_bridge.entity.enums.AssignmentStatus;

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
