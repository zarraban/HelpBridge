package com.example.help_bridge.common.security.dto.request;

import com.example.help_bridge.users.auth.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Облікові дані для входу")
public record LoginRequest(
        @Schema(description = "Тип облікового запису", example = "VOLUNTEER")
        @NotNull(message = "This field is necessary")
        AccountType accountType,

        @Schema(description = "Email користувача", example = "admin@helpbridge.org")
        @NotBlank(message = "This field is necessary")
        String email,

        @Schema(description = "Пароль", example = "Passw0rd123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "This field is necessary")
        String password
) {
    @Override
    public String toString() {
        return "LoginRequest[accountType=" + accountType + ", email=" + email + ", password=****]";
    }
}
