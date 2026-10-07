package com.example.help_bridge.fundraising.fund.dto.request;

import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Зміна статусу фонду адміністратором")
public record FundStatusUpdateRequest(

        @Schema(description = "Новий статус фонду")
        @NotNull(message = "Status is required")
        FundStatus status,

        @Schema(description = "Id системного адміністратора, який приймає рішення", example = "1")
        @NotNull(message = "Admin id is required")
        Long adminId,

        @Schema(description = "Коментар до рішення", example = "Документи перевірено, фонд схвалено")
        @Size(max = 1000)
        String comment
) {
}