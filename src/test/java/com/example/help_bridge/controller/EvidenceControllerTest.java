package com.example.help_bridge.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvidenceController.class)
public class EvidenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenValidAddEvidenceRequest_thenReturns200() throws Exception {
        String validJson = """
                {
                  "receiptNumber": "12345",
                  "recipientFeedback": "Thanks",
                  "attachmentUrl": "http://example.com/pic.jpg"
                }
                """;

        mockMvc.perform(post("/api/fundraisers/1/evidences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void whenInvalidAddEvidenceRequest_thenReturns400() throws Exception {
        String invalidJson = """
                {
                  "receiptNumber": "",
                  "recipientFeedback": "Thanks",
                  "attachmentUrl": "not-a-url"
                }
                """;

        mockMvc.perform(post("/api/fundraisers/1/evidences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
