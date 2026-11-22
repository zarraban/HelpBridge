package com.example.help_bridge.common.audit;

import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.exception.DuplicateSystemAdminException;
import com.example.help_bridge.users.systemadmin.service.SystemAdminService;
import org.junit.jupiter.api.*;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.repository.*;
import com.example.help_bridge.fundraising.fundraiser.service.EvidenceService;
import com.example.help_bridge.fundraising.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import com.example.help_bridge.common.config.DevDataSeeder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {"security.jwt.secret=test-secret-012345678901234567890123456789", "logging.level.root=WARN"})
@ActiveProfiles("dev")
class AuditBusinessIntegrationTest {
    @Autowired
    EvidenceService evidenceService;
    @Autowired
    EvidenceRepository evidences;
    @Autowired
    FundraiserRepository fundraisers;
    @Autowired
    FundraiserAssignmentRepository assignments;
    @Autowired
    VolunteerRepository volunteers;
    @Autowired
    SystemAdminService service;
    @Autowired
    AuditLogRepository logs;
    @Autowired
    org.springframework.transaction.PlatformTransactionManager transactionManager;
    @Autowired
    com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository admins;

    @BeforeEach
    void setUp() {
        logs.deleteAllInBatch();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("audit-admin", "", List.of()));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void successfulBusinessOperationPersistsMaskedPayload() {
        var dto = service.createSystemAdmin(request());
        var log = logs.findAll().getFirst();
        assertThat(log.isSuccessful()).isTrue();
        assertThat(log.getUsername()).isEqualTo("audit-admin");
        assertThat(log.getAction()).isEqualTo("SYSTEM_ADMIN_CREATED");
        assertThat(log.getArguments()).doesNotContain("Secret123!");
        assertThat(log.getResult()).contains(dto.email());
        assertThat(log.getOccurredAt()).isNotNull();
    }

    @Test
    void businessFailureSurvivesRollbackAndKeepsExceptionType() {
        var request = request();
        service.createSystemAdmin(request);
        logs.deleteAllInBatch();
        assertThatThrownBy(() -> service.createSystemAdmin(request))
                .isInstanceOf(DuplicateSystemAdminException.class);
        assertThat(logs.findAll()).hasSize(1);
        var log = logs.findAll().getFirst();
        assertThat(log.isSuccessful()).isFalse();
        assertThat(log.getExceptionType()).isEqualTo(DuplicateSystemAdminException.class.getName());
        assertThat(log.getResult()).isNull();
    }

    @Test
    void businessAuditSurvivesOuterRollback() {
        var request = request();
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            service.createSystemAdmin(request);
            status.setRollbackOnly();
        });
        assertThat(admins.existsByEmail(request.email())).isFalse();
        assertThat(logs.findAll()).hasSize(1);
    }

    @Test
    void validationFailureIsAuditedAndOriginalExceptionEscapes() {
        var volunteer = volunteers.findByEmail(DevDataSeeder.VOLUNTEER_EMAIL).orElseThrow();
        var fundraiser = new Fundraiser();
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        fundraiser = fundraisers.saveAndFlush(fundraiser);
        var assignment = new FundraiserAssignment();
        assignment.setFundraiser(fundraiser);
        assignment.setVolunteerId(volunteer.getId());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment = assignments.saveAndFlush(assignment);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(DevDataSeeder.VOLUNTEER_EMAIL, "",
                        List.of(new SimpleGrantedAuthority("ROLE_VOLUNTEER"))));
        long before = evidences.count();
        Long fundraiserId = fundraiser.getId();
        try {
            Throwable original = catchThrowable(() -> evidenceService.addEvidenceToFundraiser(
                    fundraiserId, new AddEvidenceRequest("123", null, null)));
            assertThat(original).isInstanceOf(InvalidEvidenceException.class)
                    .hasMessage("Receipt number is too short");
            assertThat(evidences.count()).isEqualTo(before);
            assertThat(logs.findAll()).hasSize(1);
            var log = logs.findAll().getFirst();
            assertThat(log.isSuccessful()).isFalse();
            assertThat(log.getAction()).isEqualTo("EVIDENCE_ADDED");
            assertThat(log.getUsername()).isEqualTo(DevDataSeeder.VOLUNTEER_EMAIL);
            assertThat(log.getArguments()).contains("123");
            assertThat(log.getExceptionType()).isEqualTo(original.getClass().getName());
            assertThat(log.getExceptionMessage()).isEqualTo(original.getMessage());
            assertThat(log.getResult()).isNull();
        } finally {
            assignments.deleteById(assignment.getId());
            fundraisers.deleteById(fundraiserId);
        }
    }

    private SystemAdminCreateRequest request() {
        return new SystemAdminCreateRequest(UUID.randomUUID() + "@test.example", "Secret123!", "Test", "Admin");
    }
}
