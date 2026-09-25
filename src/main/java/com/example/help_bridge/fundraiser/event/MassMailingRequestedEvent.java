package com.example.help_bridge.fundraiser.event;

public record MassMailingRequestedEvent(
        Long fundraiserId,
        String subject,
        String message
) {
}