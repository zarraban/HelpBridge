package com.example.help_bridge.volunteer.dto.response;

public record VolunteerResponse(
        Long id,
        Long fundId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
