package com.example.help_bridge.fundraising.fundraiser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "fundraiser")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fundraiser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Enumerated(EnumType.STRING)
    private FundraiserStatus status;

    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evidence> evidences = new ArrayList<>();

    @OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FundraiserAssignment> assignments = new ArrayList<>();
}