package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignVolunteerRequest(
        @NotNull
        Long volunteerId
) {
}
