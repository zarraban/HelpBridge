package com.example.help_bridge.fundraising.fundraiser.strategy;

import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidEvidenceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackValidatorStrategyTest {

    private final FeedbackValidatorStrategy strategy = new FeedbackValidatorStrategy();

    @Test
    void supports_onlyWhenFeedbackPresent() {
        Evidence withFeedback = new Evidence();
        withFeedback.setRecipientFeedback("Thank you!");
        Evidence photoOnly = new Evidence();
        photoOnly.setAttachmentUrl("https://example.com/a.jpg");

        assertThat(strategy.supports(withFeedback)).isTrue();
        assertThat(strategy.supports(photoOnly)).isFalse();
    }

    @Test
    void validate_whenFeedbackTooLong_throws() {
        Evidence evidence = new Evidence();
        evidence.setRecipientFeedback("x".repeat(1001));

        assertThatThrownBy(() -> strategy.validate(evidence)).isInstanceOf(InvalidEvidenceException.class);
    }
}
