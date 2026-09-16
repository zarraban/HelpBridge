package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record AddEvidenceRequest(
        @Size(min = 1, max = 50)
        String receiptNumber,

        @Size(max = 1000)
        String recipientFeedback,

        @Size(max = 500)
        @URL
        String attachmentUrl
) {
}
