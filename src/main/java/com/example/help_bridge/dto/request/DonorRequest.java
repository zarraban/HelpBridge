package com.example.help_bridge.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DonorRequest(
        @NotBlank
        @Size(min = 2, max = 100)
        String firstName,

        @NotBlank
        @Size(min = 2, max = 100)
        String lastName,

        @NotBlank
        @Email
        String email,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$")
        String phone
) {
}
