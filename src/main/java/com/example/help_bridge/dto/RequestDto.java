package com.example.help_bridge.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestDto(
        @NotBlank(message = "Assistance type is mandatory")
        String assistanceType,

        @NotNull(message = "Amount is mandatory")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Deadline is mandatory")
        @Future(message = "Deadline must be in the future")
        LocalDate deadline,

        @NotBlank(message = "Situation description is mandatory")
        @Size(max = 1000, message = "Situation description is too long")
        String situationDescription,

        @NotBlank(message = "Need description is mandatory")
        @Size(max = 1000, message = "Need description is too long")
        String needDescription,

        @NotBlank(message = "Institution name is mandatory")
        String institutionName,

        @NotBlank(message = "Application number is mandatory")
        String applicationNumber
) {}
