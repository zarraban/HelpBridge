package com.example.help_bridge.fundraiser.strategy;

import com.example.help_bridge.common.exception.InvalidEvidenceException;
import com.example.help_bridge.fundraiser.entity.Evidence;
import org.springframework.stereotype.Component;

@Component
public class ReceiptValidatorStrategy implements EvidenceValidatorStrategy{
    @Override
    public boolean supports(Evidence evidence) {
        return evidence.getReceiptNumber() !=null && !evidence.getReceiptNumber().isBlank();
    }

    @Override
    public void validate(Evidence evidence) {
        if(evidence.getReceiptNumber().length() < 5){
            throw new InvalidEvidenceException("Receipt number is too short");
        }
    }
}
