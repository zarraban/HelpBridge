package com.example.help_bridge.dto;

import jakarta.validation.constraints.NotNull;

public class RequestBookingDto {

    public record BookRequestRequest(
            @NotNull(message = "Fund ID is mandatory")
            Long fundId
    ) {}
}