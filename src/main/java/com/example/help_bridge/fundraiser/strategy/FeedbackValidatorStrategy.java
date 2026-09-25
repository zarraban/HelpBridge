package com.example.help_bridge.fundraiser.strategy;

import com.example.help_bridge.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.fundraiser.entity.Evidence;
import org.springframework.stereotype.Component;

@Component
public class FeedbackValidatorStrategy implements EvidenceValidatorStrategy{
    @Override
    public boolean supports(Evidence evidence) {
        return evidence.getAttachmentUrl() != null && !evidence.getAttachmentUrl().isBlank();    }

    @Override
    public void validate(Evidence evidence) {
        if (evidence.getRecipientFeedback().length() > 1000) {
            throw new InvalidEvidenceException("Feedback is too long!");
        }
    }
}
