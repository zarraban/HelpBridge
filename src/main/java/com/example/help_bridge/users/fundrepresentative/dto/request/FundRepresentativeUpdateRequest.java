package com.example.help_bridge.users.fundrepresentative.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FundRepresentativeUpdateRequest(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 20) String phone
) {}