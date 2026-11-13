package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.fundraising.fundraiser.dto.request.AddEvidenceRequest;
import jakarta.validation.constraints.Size;

public record EvidenceForm(
        @Size(min = 5, max = 50, message = "Номер квитанції має містити від 5 до 50 символів") String receiptNumber,
        @Size(max = 1000, message = "Відгук має містити до 1000 символів") String recipientFeedback,
        @Size(max = 500, message = "Посилання має містити до 500 символів") String attachmentUrl
) {
    public EvidenceForm {
        receiptNumber = normalize(receiptNumber);
        recipientFeedback = normalize(recipientFeedback);
        attachmentUrl = normalize(attachmentUrl);
    }
    public boolean isEmpty() { return receiptNumber == null && recipientFeedback == null && attachmentUrl == null; }
    public AddEvidenceRequest toRequest() { return new AddEvidenceRequest(receiptNumber, recipientFeedback, attachmentUrl); }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
