package com.example.help_bridge.fundraising.fundraiser.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "fundraiser")
@Data
public class Fundraiser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Enumerated(EnumType.STRING)
    private FundraiserStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    @Transient
    private List<Evidence> evidences = new ArrayList<>();
}