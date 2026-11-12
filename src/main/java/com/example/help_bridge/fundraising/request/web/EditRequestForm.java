package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.UpdateRequestRequest;
import jakarta.validation.constraints.Future;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EditRequestForm(
        @NotBlank(message = "Оберіть тип допомоги")
        String assistanceType,

        @Size(max = 200, message = "Опис типу занадто довгий (максимум 200 символів)")
        String otherAssistanceType,

        @NotNull(message = "Вкажіть суму")
        @Positive(message = "Сума має бути більшою за нуль")
        BigDecimal amount,

        @NotNull(message = "Вкажіть крайній термін")
        @Future(message = "Крайній термін має бути в майбутньому")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
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
        String applicationNumber
) {
    public static EditRequestForm from(RequestResponse request) {
        String other = null;
        String need = request.needDescription();
        if ("OTHER".equals(request.assistanceType()) && need.startsWith("[Інше: ")) {
            int end = need.indexOf("] ", 7);
            if (end >= 0) {
                other = need.substring(7, end);
                need = need.substring(end + 2);
            }
        }
        return new EditRequestForm(request.assistanceType(), other, request.amount(), request.deadline(),
                request.situationDescription(), need, request.institutionName(), request.applicationNumber());
    }

    public boolean requiresOtherText() { return "OTHER".equals(assistanceType); }

    public UpdateRequestRequest toDto() {
        String need = requiresOtherText() ? "[Інше: " + otherAssistanceType.trim() + "] " + needDescription : needDescription;
        return new UpdateRequestRequest(assistanceType, amount, deadline, situationDescription,
                need, institutionName, applicationNumber);
    }
}
