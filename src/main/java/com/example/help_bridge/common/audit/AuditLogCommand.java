package com.example.help_bridge.common.audit;

import java.time.Instant;

public record AuditLogCommand(
        String action,
        String username,
        String methodName,
        String arguments,
        String result,
        Instant occurredAt,
        boolean successful,
        String exceptionType,
        String exceptionMessage
) {
}
