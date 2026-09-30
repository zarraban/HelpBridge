package com.example.help_bridge.fundraising.fund.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record FundCreateRequest(

        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 150, message = "Fund name should be from 2 to 150 characters")
        String fundName,

        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "\\d{8}", message = "The EDRPOU code must consist of exactly 8 digits")
        String edrpou,

        @NotBlank(message = "This field is necessary")
        @Size(max = 500, message = "Bank details are too long")
        String bankDetail,

        @NotBlank(message = "This field is necessary")
        @Size(max = 255, message = "Address is too long")
        String registeredAddress,

        @NotBlank(message = "This field is necessary")
        @Size(max = 255, message = "Address is too long")
        String actualAddress,

        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "^\\+?\\d{10,13}$", message = "Incorrect phone number format")
        String phoneNumber,

        @NotBlank(message = "This field is necessary")
        @Email(message = "Incorrect email format")
        @Size(max = 100, message = "Email is too long")
        String corpEmail,

        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "^(https?://).+", message = "Website must start with http:// or https://")
        @Size(max = 255, message = "Website URL is too long")
        String website,

        Map<@NotBlank String, @Pattern(regexp = "^(https?://).+", message = "Social media link must start with http:// or https://") String> socialMediaUrls
) {
}