package com.example.help_bridge.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FundraiserController.class)
public class FundraiserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenValidSendMailingRequest_thenReturns200() throws Exception {
        String validJson = """
                {
                  "subjectOfMail": "Update on our progress",
                  "message": "We have reached our goal!"
                }
                """;

        mockMvc.perform(get("/api/fundraiser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void whenInvalidSendMailingRequest_thenReturns400() throws Exception {
        String invalidJson = """
                {
                  "subjectOfMail": "",
                  "message": ""
                }
                """;

        mockMvc.perform(post("/api/fundraiser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
