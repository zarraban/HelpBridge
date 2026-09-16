package com.example.help_bridge.dto;

import jakarta.validation.constraints.NotNull;

public record RequestVerificationDto(
        @NotNull(message = "Decision (approved/rejected) is mandatory")
        Boolean approved,

        String comment
) {}
