package com.example.help_bridge.users.volunteer.event;

public record VolunteerRegisteredEvent(
        Long fundId,
        String firstName,
        String email
) {
}
