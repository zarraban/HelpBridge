package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.fundraising.fundraiser.dto.response.EvidenceResponse;
import com.example.help_bridge.fundraising.request.web.RequestDocumentLink;
import java.time.LocalDateTime;

public record EvidenceLink(Long id, String receiptNumber, String feedback, String url, LocalDateTime createdAt) {
    public static EvidenceLink from(EvidenceResponse evidence) {
        String url = evidence.attachmentUrl();
        return new EvidenceLink(evidence.evidenceId(), evidence.receiptNumber(), evidence.recipientFeedback(),
                RequestDocumentLink.isSafeWebUrl(url) ? url.trim() : null, evidence.createdAt());
    }
}
