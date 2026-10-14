package com.example.help_bridge.common.security.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailAccessDeniedHandlerTest {

    // Spring Boot registers the same mixin on the auto-configured JsonMapper
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class)
            .build();

    private final ProblemDetailAccessDeniedHandler handler =
            new ProblemDetailAccessDeniedHandler(objectMapper);

    @Test
    void handleWritesForbiddenProblemDetail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/funds/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access Denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("title").asString()).isEqualTo("Доступ заборонено");
        assertThat(body.get("type").asString()).isEqualTo("https://api.example.com/errors/forbidden");
        assertThat(body.get("instance").asString()).isEqualTo("/api/v1/funds/1");
        assertThat(body.has("timestamp")).isTrue();
    }

    @Test
    void responseDoesNotLeakExceptionMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response,
                new AccessDeniedException("missing ROLE_ADMIN"));

        assertThat(response.getContentAsString()).doesNotContain("missing ROLE_ADMIN");
    }
}
