package com.example.help_bridge.fundraising.fundraiser.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignVolunteerForm(
        @NotNull(message = "Оберіть волонтера")
        @Positive(message = "Оберіть волонтера зі списку") Long volunteerId
) {}
