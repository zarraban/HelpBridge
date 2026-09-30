package com.example.help_bridge.users.volunteer.command;

public record RegisterVolunteerCommand(
        Long fundId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
