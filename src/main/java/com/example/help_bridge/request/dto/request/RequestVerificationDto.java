package com.example.help_bridge.request.dto.request;

import jakarta.validation.constraints.NotNull;

public record RequestVerificationDto(
        @NotNull(message = "Decision (approved/rejected) is mandatory")
        Boolean approved,

        String comment
) {}
