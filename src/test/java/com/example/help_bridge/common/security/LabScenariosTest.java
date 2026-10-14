package com.example.help_bridge.common.security;

import com.example.help_bridge.common.config.DevDataSeeder;
import com.example.help_bridge.users.auth.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:labtest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class LabScenariosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult login(AccountType type, String email) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("accountType", type.name(), "email", email, "password", DevDataSeeder.DEMO_PASSWORD));
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
    }

    @Test
    void loginReturnsTokenAndTokenOpensProtectedEndpoint() throws Exception {
        MvcResult result = login(AccountType.SYSTEM_ADMIN, DevDataSeeder.ADMIN_EMAIL);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asString();

        mockMvc.perform(get("/api/funds").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "SYSTEM_ADMIN," + DevDataSeeder.ADMIN_EMAIL,
            "FUND_REPRESENTATIVE," + DevDataSeeder.REPRESENTATIVE_EMAIL,
            "VOLUNTEER," + DevDataSeeder.VOLUNTEER_EMAIL,
            "APPLICANT," + DevDataSeeder.APPLICANT_EMAIL
    })
    void everySeededAccountCanLogIn(AccountType type, String email) throws Exception {
        MvcResult result = login(type, email);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("accessToken").asString()).isNotBlank();
        assertThat(body.get("tokenType").asString()).isEqualTo("Bearer");
    }
}
