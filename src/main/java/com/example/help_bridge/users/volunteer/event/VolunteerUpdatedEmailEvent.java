package com.example.help_bridge.users.volunteer.event;

public record VolunteerUpdatedEmailEvent(
        String firstName,
        String email
) {
}
