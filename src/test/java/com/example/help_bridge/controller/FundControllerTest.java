package com.example.help_bridge.controller;

import com.example.help_bridge.dto.FundCreateRequest;
import com.example.help_bridge.dto.FundResponse;
import com.example.help_bridge.dto.FundStatusUpdateRequest;
import com.example.help_bridge.entity.FundStatus;
import com.example.help_bridge.service.FundService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FundController.class)
class FundControllerTest {

    private static final String URL = "/funds";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FundService service;

    private FundResponse createSampleResponse() {
        return new FundResponse(
                1L,
                "Ivan",
                "Ivanov",
                "Help Fund",
                "12345678",
                "Bank Details",
                "Registered Address",
                "Actual Address",
                "+380991234567",
                "test@fund.com",
                "https://fund.com",
                Map.of("facebook", "https://facebook.com/fund"),
                "Description",
                FundStatus.PENDING_APPROVAL
        );
    }

    @Test
    void getAllFunds_ShouldReturnList() throws Exception {
        when(service.getAllFunds()).thenReturn(List.of(createSampleResponse()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].fundName").value("Help Fund"));
    }

    @Test
    void getFundById_ShouldReturnFund() throws Exception {
        when(service.getFundById(1L)).thenReturn(createSampleResponse());

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
    @Test
    void createFund_ShouldReturnResponse() throws Exception {
        String jsonRequest = """
                {
                    "fundRepresName": "Daria",
                    "fundRepresSurname": "Chorna",
                    "fundName": "Help Fund",
                    "edrpou": "12345678",
                    "bankDetail": "Bank Details",
                    "registeredAddress": "Address Reg",
                    "actualAddress": "Address Act",
                    "phoneNumber": "+380971111111",
                    "corpEmail": "test@fund.com",
                    "website": "https://fund.com",
                    "socialMediaUrl": {
                        "facebook": "https://facebook.com/fund"
                    }
                }
                """;

        when(service.createFund(any(FundCreateRequest.class))).thenReturn(createSampleResponse());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated()); // Змінено на isCreated()
    }

    @Test
    void updateFundStatus_ShouldReturnUpdatedFund() throws Exception {
        String jsonRequest = """
                {
                    "status": "APPROVED"
                }
                """;

        when(service.updateFundStatus(any(Long.class), any(FundStatusUpdateRequest.class)))
                .thenReturn(createSampleResponse());

        mockMvc.perform(patch(URL + "/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFundById_ShouldReturn200() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isOk());
    }
}