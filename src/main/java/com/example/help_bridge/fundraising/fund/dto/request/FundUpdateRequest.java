package com.example.help_bridge.fundraising.fund.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record FundUpdateRequest(
        @NotBlank @Size(max = 150) String fundName,
        @NotBlank String registeredAddress,
        @NotBlank String actualAddress,
        @NotBlank @Size(max = 20) String phoneNumber,
        @NotBlank @Email @Size(max = 100) String corpEmail,
        @NotBlank String website,
        Map<String, String> socialMediaUrls) {}
