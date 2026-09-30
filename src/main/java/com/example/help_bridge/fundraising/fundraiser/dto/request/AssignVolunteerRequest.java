package com.example.help_bridge.fundraising.fundraiser.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignVolunteerRequest(
        @NotNull(message = "Volunteer ID cannot be null")
        Long volunteerId
) {
}
