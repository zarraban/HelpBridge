package com.example.help_bridge.users.fundrepresentative.entity;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fund_representatives",
        uniqueConstraints = @UniqueConstraint(name = "uk_fund_representatives_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundRepresentative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    public FundRepresentative(String firstName, String lastName, String email,
                              String phone, String passwordHash, Fund fund) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.fund = fund;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FundRepresentative other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}