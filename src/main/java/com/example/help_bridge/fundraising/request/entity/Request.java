package com.example.help_bridge.fundraising.request.entity;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String assistanceType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(length = 1000, nullable = false)
    private String situationDescription;

    @Column(length = 1000, nullable = false)
    private String needDescription;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false, unique = true)
    private String applicationNumber;

    @Column(nullable = false)
    private boolean dataProcessingConsent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestDocument> documents = new ArrayList<>();

    protected Request() {
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING_VERIFICATION;
    }

    public Request(User requester, String assistanceType, BigDecimal amount, LocalDate deadline,
                   String situationDescription, String needDescription,
                   String institutionName, String applicationNumber,
                   boolean dataProcessingConsent) {
        this.requester = requester;
        this.assistanceType = assistanceType;
        this.amount = amount;
        this.deadline = deadline;
        this.situationDescription = situationDescription;
        this.needDescription = needDescription;
        this.institutionName = institutionName;
        this.applicationNumber = applicationNumber;
        this.dataProcessingConsent = dataProcessingConsent;
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING_VERIFICATION;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = RequestStatus.PENDING_VERIFICATION;
        }
    }

    public void updateDetails(String assistanceType, BigDecimal amount, LocalDate deadline,
                              String situationDescription, String needDescription,
                              String institutionName, String applicationNumber) {
        this.assistanceType = assistanceType;
        this.amount = amount;
        this.deadline = deadline;
        this.situationDescription = situationDescription;
        this.needDescription = needDescription;
        this.institutionName = institutionName;
        this.applicationNumber = applicationNumber;
    }

    public void addDocument(RequestDocument document) {
        documents.add(document);
        document.setRequest(this);
    }

    public void removeDocument(RequestDocument document) {
        documents.remove(document);
        document.setRequest(null);
    }

    public void transitionTo(RequestStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new InvalidRequestStateException(
                    "Cannot transition request " + id + " from " + status + " to " + nextStatus);
        }
        this.status = nextStatus;
    }

    public boolean isExpired() {
        return deadline != null && deadline.isBefore(LocalDate.now()) && status != RequestStatus.CLOSED;
    }

    public Long getId() { return id; }
    public String getAssistanceType() { return assistanceType; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDeadline() { return deadline; }
    public String getSituationDescription() { return situationDescription; }
    public String getNeedDescription() { return needDescription; }
    public String getInstitutionName() { return institutionName; }
    public String getApplicationNumber() { return applicationNumber; }
    public boolean isDataProcessingConsent() { return dataProcessingConsent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public RequestStatus getStatus() { return status; }
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    public Fund getFund() { return fund; }
    public void setFund(Fund fund) { this.fund = fund; }
    public List<RequestDocument> getDocuments() { return documents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request request)) return false;
        if (id != null && request.id != null) {
            return Objects.equals(id, request.id);
        }
        return applicationNumber != null && Objects.equals(applicationNumber, request.applicationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationNumber != null ? applicationNumber : id);
    }
}