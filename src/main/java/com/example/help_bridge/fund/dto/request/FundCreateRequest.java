package com.example.help_bridge.fund.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record FundCreateRequest(
        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 25, message = "Name should be from 2 to 25 characters")
        String fundRepresName,

        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 25, message = "Surname should be from 2 to 25 characters")
        String fundRepresSurname,

        @NotBlank(message = "This field is necessary")
        @Size(min = 2, message = "Fund name should be longer than 2 characters")
        String fundName,

        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "\\d{8}", message = "The EDRPOU code must have exactly 8 characters")
        String edrpou,

        @NotBlank(message = "This field is necessary")
        String bankDetail,

        @NotBlank(message = "This field is necessary")
        String registeredAddress,

        @NotBlank(message = "This field is necessary")
        String actualAddress,

        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "^\\+?\\d{10,13}$", message = "Incorrect phone number format")
        String phoneNumber,

        @NotBlank(message = "This field is necessary")
        @Email(message = "Incorrect input")
        String corpEmail,

        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "^(https?://).+", message = "Website must start with http:// or https://")
        String website,

        Map<String, String> socialMediaUrls
) {
}
