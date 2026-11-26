package com.example.help_bridge.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ActuatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void loggersEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/loggers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loggersAccessibleWithActuatorAdminCredentials() throws Exception {
        mockMvc.perform(get("/actuator/loggers")
                        .with(httpBasic("actuator-admin", "AdminPass123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.levels").isArray());
    }

    @Test
    void loggersAccessibleWithAdminCredentials() throws Exception {
        mockMvc.perform(get("/actuator/loggers")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.levels").isArray());
    }

    @Test
    void loggersReturnsUnauthorizedWithWrongPassword() throws Exception {
        mockMvc.perform(get("/actuator/loggers")
                        .with(httpBasic("admin", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "volunteer", roles = {"VOLUNTEER"})
    void loggersForbiddenForNonAdminRole() throws Exception {
        mockMvc.perform(get("/actuator/loggers"))
                .andExpect(status().isForbidden());
    }

    @Test
    void canChangeLogLevelWithAdminCredentials() throws Exception {
        mockMvc.perform(post("/actuator/loggers/com.example.help_bridge")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configuredLevel\":\"DEBUG\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/actuator/loggers/com.example.help_bridge")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configuredLevel").value("DEBUG"));
    }
}
