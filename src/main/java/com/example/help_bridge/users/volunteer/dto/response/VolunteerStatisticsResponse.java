package com.example.help_bridge.users.volunteer.dto.response;

import java.time.LocalDate;

public record VolunteerStatisticsResponse(
        Long volunteerId,
        LocalDate from,
        LocalDate to,
        long closedFundraisersCount
) {
}
