package com.example.help_bridge.fundraiser.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReturnAssignmentRequest(
        @NotBlank(message = "Return reason cannot be blank")
        @Size(min = 5, max = 500, message = "Return reason must be between 5 and 500 characters")
        String returnReason
) {
}
