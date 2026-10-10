package com.example.help_bridge.fundraising.fund.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record FundUpdateRequest(
        @NotBlank @Size(max = 150) String fundName,
        @NotBlank @Size(max = 255) String registeredAddress,
        @Size(max = 255) String actualAddress,
        @NotBlank @Pattern(regexp = "^\\+?\\d{10,13}$", message = "Incorrect phone number format") String phoneNumber,
        @NotBlank @Email @Size(max = 100) String corpEmail,
        @NotBlank @Pattern(regexp = "^(https?://).+", message = "Website must start with http:// or https://") @Size(max = 255) String website,
        Map<String, String> socialMediaUrls) {}
