package com.example.help_bridge.volunteer.event;

public record VolunteerUpdatedPhoneNumberEvent(
        String firstName,
        String phoneNumber,
        String email
) {
}
