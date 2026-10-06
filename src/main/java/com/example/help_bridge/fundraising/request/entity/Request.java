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
    @Version
    private Long version;

    @Column(nullable = false)
    private String assistanceType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(length = 1000, nullable = false)
    private String situationDescription;

    @Column(length = 1000, nullable = false)
    private String needDescription;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String applicationNumber;

    @Column(nullable = false)
    private boolean dataProcessingConsent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestDocument> documents = new ArrayList<>();

    protected Request() {}

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
    /** Редагувати можна лише заявку, яку адмін ще не перевірив. */
    public void updateDetails(String assistanceType, BigDecimal amount, LocalDate deadline,
                              String situationDescription, String needDescription,
                              String institutionName, String applicationNumber) {
        if (status != RequestStatus.PENDING_VERIFICATION) {
            throw new InvalidRequestStateException(
                    "Request " + id + " can be edited only in PENDING_VERIFICATION, but is " + status);
        }
        this.assistanceType = assistanceType;
        this.amount = amount;
        this.deadline = deadline;
        this.situationDescription = situationDescription;
        this.needDescription = needDescription;
        this.institutionName = institutionName;
        this.applicationNumber = applicationNumber;
    }
    public void approve() {
        transitionTo(RequestStatus.NEW);
    }
    public void book(Fund fund) {
        Objects.requireNonNull(fund, "fund must not be null");
        transitionTo(RequestStatus.IN_PROGRESS);
        this.fund = fund;
    }
    public void ensureRejectable() {
        if (status != RequestStatus.PENDING_VERIFICATION) {
            throw new InvalidRequestStateException(
                    "Request " + id + " can be rejected only in PENDING_VERIFICATION, but is " + status);
        }
    }
    public void ensureDeletable() {
        if (status != RequestStatus.PENDING_VERIFICATION && status != RequestStatus.NEW) {
            throw new InvalidRequestStateException(
                    "Request " + id + " cannot be deleted in status " + status);
        }
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
        return deadline.isBefore(LocalDate.now()) && status != RequestStatus.CLOSED;
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
    public Fund getFund() { return fund; }
    public List<RequestDocument> getDocuments() { return documents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Request.class.hashCode();
    }
}