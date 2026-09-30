package com.example.help_bridge.users.volunteer.event;

public record VolunteerUpdatedPhoneNumberEvent(
        String firstName,
        String phoneNumber,
        String email
) {
}
