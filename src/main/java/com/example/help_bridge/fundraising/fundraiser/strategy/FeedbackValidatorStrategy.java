package com.example.help_bridge.fundraising.fundraiser.strategy;

import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidEvidenceException;
import org.springframework.stereotype.Component;

@Component
public class FeedbackValidatorStrategy implements EvidenceValidatorStrategy{
    @Override
    public boolean supports(Evidence evidence) {
        return evidence.getRecipientFeedback() != null && !evidence.getRecipientFeedback().isBlank();
    }

    @Override
    public void validate(Evidence evidence) {
        if (evidence.getRecipientFeedback().length() > 1000) {
            throw new InvalidEvidenceException("Feedback is too long!");
        }
    }
}
