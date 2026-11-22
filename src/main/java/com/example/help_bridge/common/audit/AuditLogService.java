package com.example.help_bridge.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditLogCommand command) {
        repository.save(toEntity(command));
    }

    private AuditLogEntity toEntity(AuditLogCommand command) {
        return new AuditLogEntity(
                command.action(),
                command.username(),
                command.methodName(),
                command.arguments(),
                command.result(),
                command.occurredAt(),
                command.successful(),
                command.exceptionType(),
                command.exceptionMessage());
    }
}
