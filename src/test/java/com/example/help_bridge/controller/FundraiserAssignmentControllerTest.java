package com.example.help_bridge.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FundraiserAssignmentController.class)
public class FundraiserAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenValidCompleteAssignmentRequest_thenReturns200() throws Exception {
        String validJson = """
                {
                  "closingComment": "This assignment is fully completed now."
                }
                """;

        mockMvc.perform(patch("/api/fundraiser-assignments/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void whenInvalidCompleteAssignmentRequest_thenReturns400() throws Exception {
        String invalidJson = """
                {
                  "closingComment": "Short"
                }
                """;

        mockMvc.perform(patch("/api/fundraiser-assignments/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenValidReturnAssignmentRequest_thenReturns200() throws Exception {
        String validJson = """
                {
                  "returnReason": "I cannot finish this task."
                }
                """;

        mockMvc.perform(patch("/api/fundraiser-assignments/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void whenInvalidReturnAssignmentRequest_thenReturns400() throws Exception {
        String invalidJson = """
                {
                  "returnReason": "Why"
                }
                """;

        mockMvc.perform(patch("/api/fundraiser-assignments/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenValidAssignVolunteerRequest_thenReturns200() throws Exception {
        String validJson = """
                {
                  "volunteerId": 10
                }
                """;

        mockMvc.perform(post("/api/fundraisers/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void whenInvalidAssignVolunteerRequest_thenReturns400() throws Exception {
        String invalidJson = """
                {
                }
                """;

        mockMvc.perform(post("/api/fundraisers/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
