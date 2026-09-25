package com.example.help_bridge.volunteer.event;

public record VolunteerRemovedEvent (
        String firstName,
        String email
) {
}
