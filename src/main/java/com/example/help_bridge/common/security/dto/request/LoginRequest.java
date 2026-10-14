package com.example.help_bridge.common.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Облікові дані для входу")
public record LoginRequest(
        @Schema(description = "Логін користувача", example = "admin@helpbridge.org")
        @NotBlank(message = "This field is necessary")
        String username,

        @Schema(description = "Пароль", example = "Passw0rd123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "This field is necessary")
        String password
) {
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=****]";
    }
}
