package com.example.help_bridge.fundraising.request.dto.request;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class RequestDto {

    private RequestDto() {}

    @Schema(description = "Дані для створення запиту на допомогу")
    public record CreateRequestRequest(
            @Schema(description = "Id користувача, який створює запит", example = "1")
            @NotNull(message = "User ID is mandatory")
            Long userId,

            @Schema(description = "Тип допомоги", example = "MEDICAL")
            @NotBlank(message = "Assistance type is mandatory")
            String assistanceType,

            @Schema(description = "Потрібна сума, грн", example = "15000.00")
            @NotNull(message = "Amount is mandatory")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount,

            @Schema(description = "Крайній термін (у майбутньому)", example = "2027-01-15")
            @NotNull(message = "Deadline is mandatory")
            @Future(message = "Deadline must be in the future")
            LocalDate deadline,

            @Schema(description = "Опис ситуації", example = "Потрібне термінове лікування після травми")
            @NotBlank(message = "Situation description is mandatory")
            @Size(max = 1000, message = "Situation description is too long")
            String situationDescription,

            @Schema(description = "Опис потреби", example = "Оплата операції та реабілітації")
            @NotBlank(message = "Need description is mandatory")
            @Size(max = 1000, message = "Need description is too long")
            String needDescription,

            @Schema(description = "Назва закладу", example = "Київська міська лікарня №1")
            @NotBlank(message = "Institution name is mandatory")
            String institutionName,

            @Schema(description = "Номер заявки", example = "APP-2026-0001")
            @NotBlank(message = "Application number is mandatory")
            String applicationNumber,

            @Schema(description = "Згода на обробку персональних даних (має бути true)", example = "true")
            @NotNull(message = "Data processing consent is mandatory")
            @AssertTrue(message = "Consent to data processing must be given")
            Boolean dataProcessingConsent
    ) {}

    @Schema(description = "Дані для оновлення запиту")
    public record UpdateRequestRequest(
            @Schema(example = "MEDICAL")
            @NotBlank(message = "Assistance type is mandatory")
            String assistanceType,

            @Schema(example = "20000.00")
            @NotNull(message = "Amount is mandatory")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount,

            @Schema(example = "2027-02-01")
            @NotNull(message = "Deadline is mandatory")
            @Future(message = "Deadline must be in the future")
            LocalDate deadline,

            @Schema(example = "Потрібне термінове лікування після травми")
            @NotBlank(message = "Situation description is mandatory")
            @Size(max = 1000, message = "Situation description is too long")
            String situationDescription,

            @Schema(example = "Оплата операції та реабілітації")
            @NotBlank(message = "Need description is mandatory")
            @Size(max = 1000, message = "Need description is too long")
            String needDescription,

            @Schema(example = "Київська міська лікарня №1")
            @NotBlank(message = "Institution name is mandatory")
            String institutionName,

            @Schema(example = "APP-2026-0001")
            @NotBlank(message = "Application number is mandatory")
            String applicationNumber
    ) {}

    @Schema(description = "Інформація про запит на допомогу")
    public record RequestResponse(
            @Schema(example = "1") Long id,
            @Schema(example = "1") Long requesterId,
            @Schema(description = "Id фонду, що забронював запит (null, якщо ще ні)", example = "1") Long fundId,
            @Schema(example = "MEDICAL") String assistanceType,
            @Schema(example = "15000.00") BigDecimal amount,
            @Schema(example = "2027-01-15") LocalDate deadline,
            @Schema(example = "Потрібне термінове лікування після травми") String situationDescription,
            @Schema(example = "Оплата операції та реабілітації") String needDescription,
            @Schema(example = "Київська міська лікарня №1") String institutionName,
            @Schema(example = "APP-2026-0001") String applicationNumber,
            @Schema(description = "Поточний статус запиту") RequestStatus status,
            @Schema(description = "Прапорець «гарячого» запиту (true, якщо термін минув)", example = "false") boolean hot,
            @Schema(example = "2026-10-07T23:30:00") LocalDateTime createdAt,
            List<DocumentResponse> documents
    ) {}
}