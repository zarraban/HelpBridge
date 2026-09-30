package com.example.help_bridge.fundraising.fundraiser.strategy;

import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;

public interface EvidenceValidatorStrategy {
    boolean supports(Evidence evidence);

    void validate(Evidence evidence);
}
