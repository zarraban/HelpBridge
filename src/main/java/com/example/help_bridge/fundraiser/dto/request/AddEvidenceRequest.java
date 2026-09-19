package com.example.help_bridge.fundraiser.dto.request;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record AddEvidenceRequest(
        @Size(min = 1, max = 50, message = "Receipt number must be between 1 and 50 characters")
        String receiptNumber,

        @Size(max = 1000, message = "Recipient feedback must be at most 1000 characters")
        String recipientFeedback,

        @Size(max = 500, message = "Attachment URL must be at most 500 characters")
        @URL(message = "Attachment URL must be a valid URL")
        String attachmentUrl
) {
}
