package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRequestForm(
        @NotBlank(message = "Оберіть тип допомоги")
        String assistanceType,

        @Size(max = 200, message = "Опис типу занадто довгий (максимум 200 символів)")
        String otherAssistanceType,

        @NotNull(message = "Вкажіть суму")
        @Positive(message = "Сума має бути більшою за нуль")
        BigDecimal amount,

        @NotNull(message = "Вкажіть крайній термін")
        @Future(message = "Крайній термін має бути в майбутньому")
        LocalDate deadline,

        @NotBlank(message = "Опишіть ситуацію")
        @Size(max = 1000, message = "Опис ситуації занадто довгий (максимум 1000 символів)")
        String situationDescription,

        @NotBlank(message = "Вкажіть, яка саме допомога потрібна")
        @Size(max = 1000, message = "Опис потреби занадто довгий (максимум 1000 символів)")
        String needDescription,

        @NotBlank(message = "Вкажіть назву установи")
        @Size(max = 255, message = "Назва установи занадто довга (максимум 255 символів)")
        String institutionName,

        @NotBlank(message = "Вкажіть номер заяви")
        @Size(max = 100, message = "Номер заяви занадто довгий (максимум 100 символів)")
        String applicationNumber,

        @NotNull(message = "Потрібна згода на перевірку інформації")
        @AssertTrue(message = "Потрібна згода на перевірку інформації")
        Boolean dataProcessingConsent
) {
    private static final String OTHER_KEY = "OTHER";

    public static CreateRequestForm empty() {
        return new CreateRequestForm(null, null, null, null, null, null, null, null, false);
    }

    public boolean requiresOtherText() {
        return OTHER_KEY.equals(assistanceType);
    }

    public CreateRequestRequest toDto(Long requesterId) {
        String need = requiresOtherText()
                ? "[Інше: " + otherAssistanceType.trim() + "] " + needDescription
                : needDescription;
        return new CreateRequestRequest(
                requesterId,
                assistanceType,
                amount,
                deadline,
                situationDescription,
                need,
                institutionName,
                applicationNumber,
                dataProcessingConsent
        );
    }
}