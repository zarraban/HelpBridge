package com.example.help_bridge.fundraising.fundraiser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fundraiser_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FundraiserAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fundraiser_id")
    private Fundraiser fundraiser;

    @Column(name = "volunteer_id")
    private Long volunteerId;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    private LocalDateTime assignedAt;
    private LocalDateTime finishedAt;
    private String returnReason;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Evidence> evidences = new ArrayList<>();

    public Long getFundraiserId() {
        return fundraiser != null ? fundraiser.getId() : null;
    }

    public void setFundraiserId(Long fundraiserId) {
        if (fundraiserId != null) {
            if (this.fundraiser == null) {
                this.fundraiser = new Fundraiser();
            }
            this.fundraiser.setId(fundraiserId);
        } else {
            this.fundraiser = null;
        }
    }
}