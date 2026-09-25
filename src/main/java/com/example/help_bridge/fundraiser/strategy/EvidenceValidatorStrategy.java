package com.example.help_bridge.fundraiser.strategy;

import com.example.help_bridge.fundraiser.entity.Evidence;

public interface EvidenceValidatorStrategy {
    boolean supports(Evidence evidence);

    void validate(Evidence evidence);
}
