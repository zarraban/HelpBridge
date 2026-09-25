package com.example.help_bridge.volunteer.command;

public record UpdateVolunteerCommand(
        Long id,
        Long fundId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
