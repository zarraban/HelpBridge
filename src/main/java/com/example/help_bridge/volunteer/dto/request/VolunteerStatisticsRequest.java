package com.example.help_bridge.volunteer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

//Query parameters ?from=YYYY-MM-DD&to=YYYY-MM-DD
public record VolunteerStatisticsRequest(
        @NotNull(message = "Parameter 'from' is required")
        @PastOrPresent(message = "Parameter 'from' cannot be in the future")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,
        @NotNull(message = "Parameter 'to' is required")
        @PastOrPresent(message = "Parameter 'to' cannot be in the future")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
}
