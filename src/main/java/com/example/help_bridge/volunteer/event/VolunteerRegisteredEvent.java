package com.example.help_bridge.volunteer.event;

public record VolunteerRegisteredEvent(
        Long fundId,
        String firstName,
        String email
) {
}
