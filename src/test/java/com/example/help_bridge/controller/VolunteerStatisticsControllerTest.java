package com.example.help_bridge.controller;

import com.example.help_bridge.dto.VolunteerStatisticsRequest;
import com.example.help_bridge.dto.VolunteerStatisticsResponse;
import com.example.help_bridge.service.VolunteerStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VolunteerStatisticsController.class)
class VolunteerStatisticsControllerTest {

    private static final String URL = "/api/volunteers/{volunteerId}/statistics";

    private static final UUID VOLUNTEER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 9, 1);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VolunteerStatisticsService service;

    @Test
    void returns200WithStatisticsForPeriod() throws Exception {
        VolunteerStatisticsRequest period = new VolunteerStatisticsRequest(FROM, TO);
        when(service.getClosedFundraisersStatistics(VOLUNTEER_ID, period))
                .thenReturn(new VolunteerStatisticsResponse(VOLUNTEER_ID, FROM, TO, 7));

        mockMvc.perform(get(URL, VOLUNTEER_ID)
                        .param("from", "2026-01-01")
                        .param("to", "2026-09-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.volunteerId").value(VOLUNTEER_ID.toString()))
                .andExpect(jsonPath("$.from").value("2026-01-01"))
                .andExpect(jsonPath("$.to").value("2026-09-01"))
                .andExpect(jsonPath("$.closedFundraisersCount").value(7));

        verify(service).getClosedFundraisersStatistics(VOLUNTEER_ID, period);
    }

    @Test
    void returns400WhenBothParamsAreMissing() throws Exception {
        mockMvc.perform(get(URL, VOLUNTEER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.from").value("Parameter 'from' is required"))
                .andExpect(jsonPath("$.errors.to").value("Parameter 'to' is required"));

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenOnlyFromIsProvided() throws Exception {
        mockMvc.perform(get(URL, VOLUNTEER_ID)
                        .param("from", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.to").value("Parameter 'to' is required"))
                .andExpect(jsonPath("$.errors.from").doesNotExist());

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenDateIsInFuture() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(get(URL, VOLUNTEER_ID)
                        .param("from", "2026-01-01")
                        .param("to", tomorrow))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.to").value("Parameter 'to' cannot be in the future"));

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenDateHasWrongFormat() throws Exception {
        mockMvc.perform(get(URL, VOLUNTEER_ID)
                        .param("from", "01.01.2026")
                        .param("to", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.from").exists());

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenVolunteerIdIsNotUuid() throws Exception {
        mockMvc.perform(get(URL, "123")
                        .param("from", "2026-01-01")
                        .param("to", "2026-09-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
