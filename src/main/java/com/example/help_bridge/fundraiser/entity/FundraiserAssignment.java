package com.example.help_bridge.fundraiser.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FundraiserAssignment {
    private Long id;
    private Long fundraiserId;
    private Long volunteerId;
    private AssignmentStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime finishedAt;
    private String returnReason;
}
