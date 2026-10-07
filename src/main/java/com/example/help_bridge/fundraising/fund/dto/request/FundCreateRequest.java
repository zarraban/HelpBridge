package com.example.help_bridge.fundraising.fund.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "Дані для створення фонду")
public record FundCreateRequest(

        @Schema(description = "Назва фонду", example = "Фонд Допомоги Дітям")
        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 150, message = "Fund name should be from 2 to 150 characters")
        String fundName,

        @Schema(description = "Код ЄДРПОУ (8 цифр)", example = "12345678")
        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "\\d{8}", message = "The EDRPOU code must consist of exactly 8 digits")
        String edrpou,

        @Schema(description = "Банківські реквізити", example = "UA213223130000026007233566001")
        @NotBlank(message = "This field is necessary")
        @Size(max = 500, message = "Bank details are too long")
        String bankDetail,

        @Schema(description = "Юридична адреса", example = "м. Київ, вул. Хрещатик, 1")
        @NotBlank(message = "This field is necessary")
        @Size(max = 255, message = "Address is too long")
        String registeredAddress,

        @Schema(description = "Фактична адреса", example = "м. Київ, вул. Хрещатик, 1")
        @Size(max = 255, message = "Address is too long")
        String actualAddress,

        @Schema(description = "Телефон", example = "+380501234567")
        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "^\\+?\\d{10,13}$", message = "Incorrect phone number format")
        String phoneNumber,

        @Schema(description = "Корпоративний email", example = "contact@fund-help.org")
        @NotBlank(message = "This field is necessary")
        @Email(message = "Incorrect email format")
        @Size(max = 100, message = "Email is too long")
        String corpEmail,

        @Schema(description = "Вебсайт", example = "https://fund-help.org")
        @NotBlank(message = "This field is necessary")
        @Pattern(regexp = "^(https?://).+", message = "Website must start with http:// or https://")
        @Size(max = 255, message = "Website URL is too long")
        String website,

        @Schema(description = "Посилання на соцмережі (назва → URL)",
                example = "{\"facebook\": \"https://facebook.com/fundhelp\", \"instagram\": \"https://instagram.com/fundhelp\"}")
        Map<@NotBlank String, @Pattern(regexp = "^(https?://).+", message = "Social media link must start with http:// or https://") String> socialMediaUrls
) {
}