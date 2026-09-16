package com.example.help_bridge.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DonorController.class)
public class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenValidDonorRequest_thenReturns200() throws Exception {
        String validJson = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john.doe@example.com",
                  "phone": "+1234567890"
                }
                """;

        mockMvc.perform(post("/api/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void whenInvalidDonorRequest_thenReturns400() throws Exception {
        String invalidJson = """
                {
                  "firstName": "",
                  "lastName": "D",
                  "email": "not-an-email",
                  "phone": "invalid-phone"
                }
                """;

        mockMvc.perform(post("/api/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
