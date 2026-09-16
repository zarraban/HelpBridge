package com.example.help_bridge.dto;

import jakarta.validation.constraints.NotNull;

public class RequestVerificationDto {

    public record RequestAdminReviewRequest(
            @NotNull(message = "Decision (approved/rejected) is mandatory")
            Boolean approved,

            String comment
    ) {}
}