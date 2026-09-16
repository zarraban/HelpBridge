package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReturnAssignmentRequest(
        @NotBlank
        @Size(min = 5, max = 500)
        String returnReason
) {
}
