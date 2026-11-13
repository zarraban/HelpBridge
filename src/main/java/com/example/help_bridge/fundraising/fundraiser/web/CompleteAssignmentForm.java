package com.example.help_bridge.fundraising.fundraiser.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteAssignmentForm(
        @NotBlank(message = "Додайте коментар завершення")
        @Size(min = 10, max = 500, message = "Коментар має містити від 10 до 500 символів") String closingComment
) {}
