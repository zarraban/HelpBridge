package com.example.help_bridge.fundraiser.dto.response;

import com.example.help_bridge.fundraiser.entity.AssignmentStatus;

import java.time.LocalDateTime;

public record ReturnAssignmentResponse(
        Long assignmentId,
        Long fundraiserId,
        AssignmentStatus status,
        LocalDateTime finishedAt,
        String returnReason
) {
}
