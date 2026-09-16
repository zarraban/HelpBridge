package com.example.help_bridge.dto;

import java.util.UUID;

public record VolunteerResponse(
        UUID id,
        UUID fundId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
