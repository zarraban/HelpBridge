package com.example.help_bridge.fundraising.fundraiser.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "fundraiser_assignment")
@Data
public class FundraiserAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fundraiser_id")
    private Long fundraiserId;

    @Column(name = "volunteer_id")
    private Long volunteerId;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    private LocalDateTime assignedAt;
    private LocalDateTime finishedAt;
    private String returnReason;
}