package com.example.help_bridge.users.volunteer.event;

public record VolunteerRemovedEvent (
        String firstName,
        String email
) {
}
