package com.example.help_bridge.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditAspectTest {

    private RecordingAuditLogService auditLogService;
    private AuditAspect aspect;
    private SampleService proxy;

    static class RecordingAuditLogService extends AuditLogService {
        AuditLogCommand lastCommand;
        RuntimeException throwOnSave;

        RecordingAuditLogService() {
            super(null);
        }

        @Override
        public void save(AuditLogCommand command) {
            if (throwOnSave != null) {
                throw throwOnSave;
            }
            this.lastCommand = command;
        }
    }

    public static class SampleService {

        @AuditAction("DO_SOMETHING")
        public String doSomething(String name, int age) {
            return "Hello " + name + ", age " + age;
        }

        @AuditAction("FAIL_OPERATION")
        public String failOperation() {
            throw new IllegalStateException("Business logic failed");
        }

        @AuditAction("SENSITIVE_OPERATION")
        public String sensitiveOperation(String password, String card, String account) {
            return "password=" + password;
        }

        @AuditAction("VOID_OPERATION")
        public void voidOperation() {
        }

        @AuditAction
        public String defaultActionName() {
            return "ok";
        }
    }

    @BeforeEach
    void setUp() {
        auditLogService = new RecordingAuditLogService();
        aspect = new AuditAspect(auditLogService, new ObjectMapper());

        AspectJProxyFactory factory = new AspectJProxyFactory(new SampleService());
        factory.addAspect(aspect);
        proxy = factory.getProxy();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void capturesSuccessfulCallWithAuthenticatedUser() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user@example.com", "password", AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = proxy.doSomething("Alice", 25);

        assertThat(result).isEqualTo("Hello Alice, age 25");

        AuditLogCommand cmd = auditLogService.lastCommand;
        assertThat(cmd).isNotNull();
        assertThat(cmd.action()).isEqualTo("DO_SOMETHING");
        assertThat(cmd.username()).isEqualTo("user@example.com");
        assertThat(cmd.methodName()).isEqualTo("SampleService.doSomething");
        assertThat(cmd.arguments()).contains("Alice").contains("25");
        assertThat(cmd.result()).isEqualTo("Hello Alice, age 25");
        assertThat(cmd.successful()).isTrue();
        assertThat(cmd.exceptionType()).isNull();
        assertThat(cmd.exceptionMessage()).isNull();
        assertThat(cmd.occurredAt()).isNotNull();
    }

    @Test
    void usesAnonymousWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        proxy.doSomething("Bob", 30);

        assertThat(auditLogService.lastCommand).isNotNull();
        assertThat(auditLogService.lastCommand.username()).isEqualTo("anonymous");
    }

    @Test
    void usesAnonymousWhenAnonymousAuthenticationToken() {
        var token = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(token);

        proxy.doSomething("Charlie", 35);

        assertThat(auditLogService.lastCommand).isNotNull();
        assertThat(auditLogService.lastCommand.username()).isEqualTo("anonymous");
    }

    @Test
    void capturesFailureAndRethrowsOriginalException() {
        assertThatThrownBy(() -> proxy.failOperation())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Business logic failed");

        AuditLogCommand cmd = auditLogService.lastCommand;
        assertThat(cmd).isNotNull();
        assertThat(cmd.action()).isEqualTo("FAIL_OPERATION");
        assertThat(cmd.successful()).isFalse();
        assertThat(cmd.result()).isNull();
        assertThat(cmd.exceptionType()).isEqualTo(IllegalStateException.class.getName());
        assertThat(cmd.exceptionMessage()).isEqualTo("Business logic failed");
    }

    @Test
    void masksSensitiveDataInArgumentsAndResult() {
        proxy.sensitiveOperation("secret123", "4111 1111 1111 1111", "UA213223130000026007233566001");

        AuditLogCommand cmd = auditLogService.lastCommand;
        assertThat(cmd).isNotNull();
        assertThat(cmd.arguments()).doesNotContain("secret123");
        assertThat(cmd.arguments()).doesNotContain("26007233566001");
        assertThat(cmd.arguments()).contains("UA****");
        assertThat(cmd.arguments()).contains("****");
        assertThat(cmd.result()).isEqualTo("password=****");
    }

    @Test
    void preservesOriginalExceptionWhenAuditServiceFailsOnException() {
        auditLogService.throwOnSave = new RuntimeException("Audit DB down");

        assertThatThrownBy(() -> proxy.failOperation())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Business logic failed")
                .hasSuppressedException(new RuntimeException("Audit DB down"));
    }

    @Test
    void voidMethodHasNullResult() {
        proxy.voidOperation();

        assertThat(auditLogService.lastCommand).isNotNull();
        assertThat(auditLogService.lastCommand.result()).isNull();
        assertThat(auditLogService.lastCommand.successful()).isTrue();
    }

    @Test
    void fallsBackToMethodNameWhenActionValueEmpty() {
        proxy.defaultActionName();

        assertThat(auditLogService.lastCommand).isNotNull();
        assertThat(auditLogService.lastCommand.action()).isEqualTo("defaultActionName");
    }
}
