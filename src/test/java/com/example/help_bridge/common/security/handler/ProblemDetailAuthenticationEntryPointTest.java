package com.example.help_bridge.common.security.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailAuthenticationEntryPointTest {

    // Spring Boot registers the same mixin on the auto-configured JsonMapper
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class)
            .build();

    private final ProblemDetailAuthenticationEntryPoint entryPoint =
            new ProblemDetailAuthenticationEntryPoint(objectMapper);

    @Test
    void commenceWritesUnauthorizedProblemDetail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/funds");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Invalid token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("title").asString()).isEqualTo("Unauthorized");
        assertThat(body.get("type").asString()).isEqualTo("https://api.example.com/errors/unauthorized");
        assertThat(body.get("instance").asString()).isEqualTo("/api/v1/funds");
        assertThat(body.has("timestamp")).isTrue();
    }

    @Test
    void responseDoesNotLeakExceptionMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response,
                new BadCredentialsException("internal signature details"));

        assertThat(response.getContentAsString()).doesNotContain("internal signature details");
    }
}
