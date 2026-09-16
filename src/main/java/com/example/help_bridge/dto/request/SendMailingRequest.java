package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.Size;

public record SendMailingRequest(
        @Size(min = 1, max = 150)
        String subjectOfMail,

        @Size(min = 1, max = 5000)
        String message
) {
}
