package com.example.help_bridge.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "action", nullable = false, columnDefinition = "TEXT")
    private String action;

    @Column(name = "username", nullable = false, columnDefinition = "TEXT")
    private String username;

    @Column(name = "method_name", nullable = false, columnDefinition = "TEXT")
    private String methodName;

    @Column(name = "arguments", columnDefinition = "TEXT")
    private String arguments;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "exception_type", columnDefinition = "TEXT")
    private String exceptionType;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    public AuditLogEntity(
            String action,
            String username,
            String methodName,
            String arguments,
            String result,
            Instant occurredAt,
            boolean successful,
            String exceptionType,
            String exceptionMessage) {
        this.action = action;
        this.username = username;
        this.methodName = methodName;
        this.arguments = arguments;
        this.result = result;
        this.occurredAt = occurredAt;
        this.successful = successful;
        this.exceptionType = exceptionType;
        this.exceptionMessage = exceptionMessage;
    }
}
