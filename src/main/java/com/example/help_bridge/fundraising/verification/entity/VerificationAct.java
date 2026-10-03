package com.example.help_bridge.fundraising.verification.entity;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "verification_acts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationAct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "system_admin_id", nullable = false)
    private SystemAdmin systemAdmin;

    @Column(nullable = false)
    private LocalDate dateOfVerification;

    public VerificationAct(Fund fund, SystemAdmin systemAdmin, LocalDate dateOfVerification) {
        this.fund = fund;
        this.systemAdmin = systemAdmin;
        this.dateOfVerification = dateOfVerification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationAct other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}