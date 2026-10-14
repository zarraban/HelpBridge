package com.example.help_bridge.common.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат успішної автентифікації")
public record AuthResponse(
        @Schema(description = "Токен доступу (JWT)", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
        @Schema(description = "Тип токена", example = "Bearer") String tokenType,
        @Schema(description = "Час життя токена в секундах", example = "900") long expiresIn
) {
    public static AuthResponse of(String token, long expiresIn) {
        return new AuthResponse(token, "Bearer", expiresIn);
    }

    @Override
    public String toString() {
        return "AuthResponse[accessToken=****, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
