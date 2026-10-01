package com.example.help_bridge.fundraising.fund.entity;

import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "funds", uniqueConstraints = @UniqueConstraint(name = "uk_funds_edrpou", columnNames = "edrpou"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String fundName;

    @Column(nullable = false, length = 8)
    private String edrpou;

    @Column(nullable = false, length = 500)
    private String bankDetail;

    @Column(nullable = false)
    private String registeredAddress;

    @Column(nullable = false)
    private String actualAddress;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String corpEmail;

    @Column(nullable = false)
    private String website;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> socialMediaUrls = new HashMap<>();

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FundStatus status = FundStatus.PENDING_APPROVAL;

    @OneToMany(mappedBy = "fund", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FundRepresentative> representatives = new HashSet<>();

    public Fund(String fundName, String edrpou,
                String bankDetail, String registeredAddress, String actualAddress,
                String phoneNumber, String corpEmail, String website,
                Map<String, String> socialMediaUrls) {
        this.fundName = fundName;
        this.edrpou = edrpou;
        this.bankDetail = bankDetail;
        this.registeredAddress = registeredAddress;
        this.actualAddress = actualAddress;
        this.phoneNumber = phoneNumber;
        this.corpEmail = corpEmail;
        this.website = website;
        if (socialMediaUrls != null) {
            this.socialMediaUrls.putAll(socialMediaUrls);
        }
    }

    public void addRepresentative(FundRepresentative representative) {
        representatives.add(representative);
        representative.setFund(this);
    }

    public void removeRepresentative(FundRepresentative representative) {
        representatives.remove(representative);
        representative.setFund(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fund other)) return false;
        return edrpou != null && edrpou.equals(other.getEdrpou());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(edrpou);
    }
}