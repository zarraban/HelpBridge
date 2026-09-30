package com.example.help_bridge.fundraising.request.dto.request;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RequestDto {

    public record CreateRequestRequest(
            Long userId,

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
            String applicationNumber,

            @NotNull(message = "Data processing consent is mandatory")
            @AssertTrue(message = "Consent to data processing must be given")
            Boolean dataProcessingConsent
    ) {}

    public record RequestResponse(
            Long id,
            String assistanceType,
            BigDecimal amount,
            LocalDate deadline,
            String situationDescription,
            String needDescription,
            String institutionName,
            String applicationNumber,
            RequestStatus status,
            boolean isHot,
            LocalDateTime createdAt,
            List<DocumentResponse> documents
    ) {}
}