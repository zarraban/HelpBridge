package com.example.help_bridge.volunteer.dto;

import java.time.LocalDate;
import java.util.UUID;

public record VolunteerStatisticsResponse(
        UUID volunteerId,
        LocalDate from,
        LocalDate to,
        long closedFundraisersCount
) {
}
