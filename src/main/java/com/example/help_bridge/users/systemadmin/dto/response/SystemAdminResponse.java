package com.example.help_bridge.users.systemadmin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Інформація про системного адміністратора")
public record SystemAdminResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "admin@helpbridge.org") String email,
        @Schema(example = "Олександр") String firstName,
        @Schema(example = "Іваненко") String lastName
) {
}