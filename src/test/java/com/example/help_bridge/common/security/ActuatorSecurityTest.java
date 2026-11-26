package com.example.help_bridge.common.security;

import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "security.jwt.secret=test-secret-test-secret-test-secret-0123456789")
@AutoConfigureMockMvc
class ActuatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SystemAdminRepository admins;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LoggingSystem loggingSystem;

    @Test
    void healthEndpointIsPublicWithoutDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void infoEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
    }

    @Test
    void loggersEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/loggers"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.startsWith("Basic")));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void loggersAccessibleToSystemAdmin() throws Exception {
        mockMvc.perform(get("/actuator/loggers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.levels").isArray());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void systemAdminSeesHealthDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    @WithMockUser(roles = "VOLUNTEER")
    void loggersForbiddenForVolunteer() throws Exception {
        mockMvc.perform(get("/actuator/loggers")).andExpect(status().isForbidden());
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void legacyAdminRoleDoesNotGrantManagementAccess() throws Exception {
        mockMvc.perform(get("/actuator/loggers")).andExpect(status().isForbidden());
    }

    @Test
    void httpBasicUsesDatabaseAdminAndRejectsWrongPassword() throws Exception {
        String email = "actuator-" + UUID.randomUUID() + "@test.example";
        String password = UUID.randomUUID().toString();
        var admin = admins.saveAndFlush(new SystemAdmin(email, passwordEncoder.encode(password), "Test", "Admin"));
        try {
            mockMvc.perform(get("/actuator/loggers").with(httpBasic(email, password)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.levels").isArray());
            mockMvc.perform(get("/actuator/loggers").with(httpBasic(email, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        } finally {
            admins.deleteById(admin.getId());
        }
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void canChangeLogLevelAndRestoreOriginalConfiguration() throws Exception {
        String logger = "com.example.help_bridge";
        var original = loggingSystem.getLoggerConfiguration(logger).getConfiguredLevel();
        try {
            mockMvc.perform(post("/actuator/loggers/" + logger)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"configuredLevel\":\"DEBUG\"}"))
                    .andExpect(status().isNoContent());
            mockMvc.perform(get("/actuator/loggers/" + logger))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.configuredLevel").value("DEBUG"));
        } finally {
            loggingSystem.setLogLevel(logger, original);
        }
    }
}
