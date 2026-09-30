package com.example.help_bridge.users.systemadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemAdminUpdateRequest(
        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 50, message = "First name should be from 2 to 50 characters")
        String firstName,

        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 50, message = "Last name should be from 2 to 50 characters")
        String lastName
) {
}