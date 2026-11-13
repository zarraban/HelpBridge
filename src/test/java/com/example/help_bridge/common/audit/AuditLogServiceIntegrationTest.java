package com.example.help_bridge.common.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(AuditLogService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AuditLogServiceIntegrationTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-11-13T10:15:30.123456Z");

    @Autowired
    private AuditLogService service;

    @Autowired
    private AuditLogRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcClient jdbc;

    @AfterEach
    void cleanUp() {
        repository.deleteAllInBatch();
    }

    @Test
    void savesSuccessfulOperationWithFullPayloadAndTimestamp() {
        String arguments = "{\"description\":\"" + "Допомога".repeat(100) + "\"}";
        String result = "{\"description\":\"" + "Збережено".repeat(100) + "\"}";
        AuditLogCommand command = new AuditLogCommand(
                "UPDATE_REQUEST", "volunteer@example.com", "RequestServiceImpl.updateRequest",
                arguments, result, OCCURRED_AT, true, null, null);

        service.save(command);

        AuditLogEntity saved = onlyLog();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAction()).isEqualTo(command.action());
        assertThat(saved.getUsername()).isEqualTo(command.username());
        assertThat(saved.getMethodName()).isEqualTo(command.methodName());
        assertThat(saved.getArguments()).isEqualTo(arguments);
        assertThat(saved.getResult()).isEqualTo(result);
        assertThat(saved.getOccurredAt()).isEqualTo(OCCURRED_AT);
        assertThat(saved.isSuccessful()).isTrue();
        assertThat(saved.getExceptionType()).isNull();
        assertThat(saved.getExceptionMessage()).isNull();
        assertThat(jdbc.sql("select count(*) from audit_log").query(Long.class).single()).isEqualTo(1L);
    }

    @Test
    void savesVoidOperationWithoutAnExistingTransaction() {
        service.save(new AuditLogCommand(
                "CLOSE_FUNDRAISER", "admin@example.com", "FundraiserServiceImpl.closeFundraiser",
                "[42]", null, OCCURRED_AT, true, null, null));

        assertThat(onlyLog().getResult()).isNull();
        assertThat(onlyLog().isSuccessful()).isTrue();
    }

    @Test
    void auditSurvivesBusinessRollbackAndOriginalExceptionEscapes() {
        var original = new IllegalStateException("Cannot close a fundraiser without evidence");
        var transaction = new TransactionTemplate(transactionManager);
        long before = fundraiserCount();

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            insertBusinessRow();
            service.save(new AuditLogCommand(
                    "CLOSE_FUNDRAISER", "admin@example.com", "FundraiserServiceImpl.closeFundraiser",
                    "[42]", null, OCCURRED_AT, false,
                    original.getClass().getName(), original.getMessage()));
            throw original;
        })).isSameAs(original);

        assertThat(fundraiserCount()).isEqualTo(before);
        AuditLogEntity saved = onlyLog();
        assertThat(saved.isSuccessful()).isFalse();
        assertThat(saved.getExceptionType()).isEqualTo(original.getClass().getName());
        assertThat(saved.getExceptionMessage()).isEqualTo(original.getMessage());
        assertThat(saved.getResult()).isNull();
    }

    @Test
    void auditCommitsEvenWhenOuterTransactionIsAlreadyRollbackOnly() {
        var transaction = new TransactionTemplate(transactionManager);
        long before = fundraiserCount();

        transaction.executeWithoutResult(status -> {
            insertBusinessRow();
            status.setRollbackOnly();
            service.save(new AuditLogCommand(
                    "ADD_EVIDENCE", "volunteer@example.com", "EvidenceServiceImpl.addEvidenceToFundraiser",
                    "[]", null, OCCURRED_AT, false, "InvalidEvidenceException", "Invalid evidence"));
        });

        assertThat(fundraiserCount()).isEqualTo(before);
        assertThat(onlyLog().getAction()).isEqualTo("ADD_EVIDENCE");
    }

    private AuditLogEntity onlyLog() {
        var logs = repository.findAll();
        assertThat(logs).hasSize(1);
        return logs.getFirst();
    }

    private void insertBusinessRow() {
        jdbc.sql("insert into fundraiser (request_id, status, created_at) values (42, 'IN_PROGRESS', CURRENT_TIMESTAMP)")
                .update();
    }

    private long fundraiserCount() {
        return jdbc.sql("select count(*) from fundraiser").query(Long.class).single();
    }
}
