package com.example.help_bridge.fundraiser.strategy;

import com.example.help_bridge.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.fundraiser.entity.Evidence;
import org.springframework.stereotype.Component;

@Component
public class PhotoValidatorStrategy implements EvidenceValidatorStrategy{
    @Override
    public boolean supports(Evidence evidence) {
        return evidence.getAttachmentUrl() != null && !evidence.getAttachmentUrl().isBlank();    }

    @Override
    public void validate(Evidence evidence) {
        String url = evidence.getAttachmentUrl();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new InvalidEvidenceException("Url link for photo should start from http:// or https://!");
        }
        if (!url.endsWith(".jpg") && !url.endsWith(".jpeg") && !url.endsWith(".png")) {
            throw new InvalidEvidenceException("Photos are allowed in following formats only: .jpg, .jpeg or .png!");
        }
    }
}
