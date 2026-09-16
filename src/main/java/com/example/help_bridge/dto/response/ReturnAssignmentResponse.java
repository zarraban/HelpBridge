package com.example.help_bridge.dto.response;

import com.example.help_bridge.entity.enums.AssignmentStatus;

import java.time.LocalDateTime;

public record ReturnAssignmentResponse(
        Long assignmentId,
        Long fundraiserId,
        AssignmentStatus status,
        LocalDateTime finishedAt,
        String returnReason
) {
}
