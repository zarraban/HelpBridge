package com.example.help_bridge.request.entity;

import com.example.help_bridge.request.exception.InvalidRequestStateException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Request {

    private final Long id;
    private final String assistanceType;
    private final BigDecimal amount;
    private final LocalDate deadline;
    private final String situationDescription;
    private final String needDescription;
    private final String institutionName;
    private final String applicationNumber;
    private RequestStatus status;

    public Request(Long id, String assistanceType, BigDecimal amount, LocalDate deadline,
                   String situationDescription, String needDescription,
                   String institutionName, String applicationNumber) {
        this.id = id;
        this.assistanceType = assistanceType;
        this.amount = amount;
        this.deadline = deadline;
        this.situationDescription = situationDescription;
        this.needDescription = needDescription;
        this.institutionName = institutionName;
        this.applicationNumber = applicationNumber;
        this.status = RequestStatus.PENDING_VERIFICATION;
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
    public RequestStatus getStatus() { return status; }
}