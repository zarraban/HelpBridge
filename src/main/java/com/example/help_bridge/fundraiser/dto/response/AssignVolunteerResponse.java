package com.example.help_bridge.fundraiser.dto.response;

import com.example.help_bridge.fundraiser.entity.AssignmentStatus;

import java.time.LocalDateTime;

public record AssignVolunteerResponse (
        Long assignmentId,
        Long fundraiserId,
        AssignmentStatus assignmentStatus,
        LocalDateTime assignedAt

){
}
