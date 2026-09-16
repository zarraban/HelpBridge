package com.example.help_bridge.dto.response;

import java.time.LocalDateTime;

public record EvidenceResponse(
        Long evidenceId,
        Long fundraiserId,
        String receiptNumber,
        String recipientFeedback,
        String attachmentUrl,
        LocalDateTime createdAt
) {
}
