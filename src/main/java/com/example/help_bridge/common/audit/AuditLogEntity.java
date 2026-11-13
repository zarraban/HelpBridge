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
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String action;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String username;

    @Column(name = "method_name", nullable = false, columnDefinition = "TEXT")
    private String methodName;

    @Column(columnDefinition = "TEXT")
    private String arguments;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private boolean successful;

    @Column(name = "exception_type", columnDefinition = "TEXT")
    private String exceptionType;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    public AuditLogEntity(AuditLogCommand command) {
        action = command.action();
        username = command.username();
        methodName = command.methodName();
        arguments = command.arguments();
        result = command.result();
        occurredAt = command.occurredAt();
        successful = command.successful();
        exceptionType = command.exceptionType();
        exceptionMessage = command.exceptionMessage();
    }
}
