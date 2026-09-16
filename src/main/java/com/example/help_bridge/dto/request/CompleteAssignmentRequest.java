package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteAssignmentRequest(
        @Size(min = 10, max = 500, message = "Closing comment must be between 10 and 500 characters")
        @NotBlank(message = "Closing comment cannot be blank")
        String closingComment
) {
}
