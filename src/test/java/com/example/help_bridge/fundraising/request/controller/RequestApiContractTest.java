package com.example.help_bridge.fundraising.request.controller;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.RequestDocumentNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.service.RequestDocumentService;
import com.example.help_bridge.fundraising.request.service.RequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RequestController.class, RequestDocumentController.class})
class RequestApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestService requestService;

    @MockitoBean
    private RequestDocumentService documentService;

    private String json(String extraFields) {
        return """
                {
                  "userId": 1,
                  "assistanceType": "MEDICAL",
                  "amount": 1500.00,
                  "deadline": "%s",
                  "situationDescription": "s",
                  "needDescription": "n",
                  "institutionName": "inst",
                  "applicationNumber": "A-1",
                  "dataProcessingConsent": true%s
                }
                """.formatted(LocalDate.now().plusDays(10), extraFields);
    }

    private RequestResponse sampleResponse() {
        return new RequestResponse(1L, 1L, null, "MEDICAL", new BigDecimal("1500.00"),
                LocalDate.now().plusDays(10), "s", "n", "inst", "A-1",
                RequestStatus.PENDING_VERIFICATION, false, LocalDateTime.now(), List.of());
    }

    @Test
    void createRequest_withValidBody_returns201AndLocation() throws Exception {
        when(requestService.createRequest(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/requests/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createRequest_withUnknownField_returns400ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(", \"unexpected\": \"x\"")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Malformed Request"));
    }

    @Test
    void createRequest_withoutUserId_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("").replace("\"userId\": 1,", "")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.userId").value("User ID is mandatory"));
    }

    @Test
    void getRequests_withBlankInstitutionName_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/requests").param("institutionName", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Constraint Violation"));
    }

    @Test
    void getRequestById_whenMissing_returns404ProblemDetail() throws Exception {
        when(requestService.getRequestById(99L)).thenThrow(new RequestNotFoundException(99L));

        mockMvc.perform(get("/api/v1/requests/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void deleteRequest_whenBooked_returns409ProblemDetail() throws Exception {
        doThrow(new InvalidRequestStateException("Request 1 cannot be deleted in status IN_PROGRESS"))
                .when(requestService).deleteRequest(1L);

        mockMvc.perform(delete("/api/v1/requests/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid Request State"));
    }

    @Test
    void getDocument_whenMissing_returns404ProblemDetail() throws Exception {
        when(documentService.getDocument(1L, 5L)).thenThrow(new RequestDocumentNotFoundException(5L));

        mockMvc.perform(get("/api/v1/requests/1/documents/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}