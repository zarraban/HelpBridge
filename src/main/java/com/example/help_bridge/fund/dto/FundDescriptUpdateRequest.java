package com.example.help_bridge.fund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FundDescriptUpdateRequest(
        @NotBlank(message = "This field is necessary")
        @Size(max = 2000, message = "Description is too long")
        String description
) {
}
