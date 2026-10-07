package com.example.help_bridge.fundraising.request.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Рішення адміністратора щодо запиту")
public record RequestVerificationDto(
        @Schema(description = "true — схвалити, false — відхилити", example = "true")
        @NotNull(message = "Decision (approved/rejected) is mandatory")
        Boolean approved,

        @Schema(description = "Коментар до рішення", example = "Документи підтверджено")
        String comment
) {}