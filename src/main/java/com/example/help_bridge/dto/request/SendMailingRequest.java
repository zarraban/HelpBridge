package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.Size;

public record SendMailingRequest(
        @Size(min = 1, max = 150, message = "Subject of mail must be between 1 and 150 characters")
        String subjectOfMail,

        @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
        String message
) {
}
