package com.example.help_bridge.fundraising.fundraiser.dto.response;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;

import java.time.LocalDateTime;

public record AssignVolunteerResponse (
        Long assignmentId,
        Long fundraiserId,
        AssignmentStatus assignmentStatus,
        LocalDateTime assignedAt

){
}
