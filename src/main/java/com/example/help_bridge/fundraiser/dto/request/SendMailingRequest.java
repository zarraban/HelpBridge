package com.example.help_bridge.fundraiser.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMailingRequest(
        @NotNull(message = "Fundraiser ID is required")
        Long fundraiserId,

        @Size(min = 1, max = 150, message = "Subject of mail must be between 1 and 150 characters")
        String subjectOfMail,

        @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
        String message
) {
}
