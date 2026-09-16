package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteAssignmentRequest(
        @Size(min = 10, max = 500)
        @NotBlank
        String closingComment
) {
}
