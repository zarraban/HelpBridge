package com.example.help_bridge.fundraising.fundraiser.event;

public record MassMailingRequestedEvent(
        Long fundraiserId,
        String subject,
        String message
) {
}