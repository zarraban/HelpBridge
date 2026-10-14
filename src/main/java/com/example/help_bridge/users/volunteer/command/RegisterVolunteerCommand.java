package com.example.help_bridge.users.volunteer.command;

public record RegisterVolunteerCommand(
        Long fundId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String password
) {
    @Override
    public String toString() {
        return "RegisterVolunteerCommand[fundId=" + fundId + ", firstName=" + firstName
                + ", lastName=" + lastName + ", email=" + email + ", phone=" + phone + ", password=****]";
    }
}
