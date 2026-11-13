package com.example.help_bridge.fundraising.fundraiser.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReturnAssignmentForm(
        @NotBlank(message = "Вкажіть причину повернення")
        @Size(min = 5, max = 500, message = "Причина має містити від 5 до 500 символів") String returnReason
) {}
