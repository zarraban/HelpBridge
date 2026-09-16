package com.example.help_bridge.dto;

import jakarta.validation.constraints.NotNull;

public record RequestAdminReviewRequest(
        @NotNull(message = "Decision (approved/rejected) is mandatory")
        Boolean approved,

        String comment
) {}
