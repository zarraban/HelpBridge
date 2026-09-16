package com.example.help_bridge.controller;

import com.example.help_bridge.dto.RequestVerificationDto.RequestAdminReviewRequest;
import com.example.help_bridge.dto.RequestBookingDto.BookRequestRequest;
import com.example.help_bridge.dto.RequestDto.CreateRequestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HelpBridgeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private static Long createdRequestId;

    @Test
    @Order(1)
    void createRequest_shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
        // Спроба створити заявку з невалідними даними (наприклад, від'ємна сума та минулий дедлайн)
        CreateRequestRequest invalidRequest = new CreateRequestRequest(
                "",
                BigDecimal.valueOf(-100),
                LocalDate.now().minusDays(1),
                "",
                "",
                "",
                ""
        );

        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    @Order(2)
    void createRequest_shouldSucceed_whenDataIsValid() throws Exception {
        CreateRequestRequest validRequest = new CreateRequestRequest(
                "Medical",
                BigDecimal.valueOf(5000.00),
                LocalDate.now().plusDays(10),
                "Critical medical situation description",
                "Need medicines urgently",
                "City Hospital #1",
                "APP-2026-001"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
                .andReturn();

        // Витягуємо ID створеної заявки з відповіді для наступних кроків
        String responseBody = result.getResponse().getContentAsString();
        createdRequestId = objectMapper.readTree(responseBody).get("id").asLong();
    }

    @Test
    @Order(3)
    void sharedPool_shouldBeEmpty_beforeAdminVerification() throws Exception {
        mockMvc.perform(get("/api/v1/requests/shared-pool"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(4)
    void verifyRequest_shouldApproveRequest() throws Exception {
        RequestAdminReviewRequest reviewRequest = new RequestAdminReviewRequest(
                true,
                "Approved by admin"
        );

        mockMvc.perform(patch("/api/v1/admin/requests/{id}/verify", createdRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(5)
    void sharedPool_shouldContainRequest_afterAdminVerification() throws Exception {
        // Тепер заявка має статус NEW і з'явилася в загальному пулі
        mockMvc.perform(get("/api/v1/requests/shared-pool"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(createdRequestId));
    }

    @Test
    @Order(6)
    void bookRequest_shouldSucceed_onFirstAttempt() throws Exception {
        BookRequestRequest bookRequest = new BookRequestRequest(101L);

        mockMvc.perform(post("/api/v1/fund/requests/{id}/book", createdRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    void bookRequest_shouldReturnConflict_whenBookedAgain() throws Exception {
        BookRequestRequest bookRequest = new BookRequestRequest(102L);

        mockMvc.perform(post("/api/v1/fund/requests/{id}/book", createdRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict State"));
    }

    @Test
    @Order(8)
    void verifyRequest_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        long nonExistentId = 99999L;
        RequestAdminReviewRequest reviewRequest = new RequestAdminReviewRequest(true, "Comment");

        mockMvc.perform(patch("/api/v1/admin/requests/{id}/verify", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}