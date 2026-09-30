package com.example.help_bridge.users.volunteer.dto.response;

public record VolunteerResponse(
        Long id,
        Long fundId,
        String firstName,
        String lastName,
        String email,
        String phone
) {
}
