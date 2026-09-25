package com.example.help_bridge.volunteer.event;

public record VolunteerUpdatedEmailEvent(
        String firstName,
        String email
) {
}
