package com.example.help_bridge.users.systemadmin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Дані для створення системного адміністратора")
public record SystemAdminCreateRequest(
        @Schema(description = "Email", example = "admin@helpbridge.org")
        @NotBlank(message = "This field is necessary")
        @Email(message = "Incorrect email format")
        @Size(max = 100, message = "Email is too long")
        String email,

        @Schema(description = "Пароль (8–72 символи)", example = "Passw0rd123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "This field is necessary")
        @Size(min = 8, max = 72, message = "Password should be from 8 to 72 characters")
        String password,

        @Schema(description = "Ім'я", example = "Олександр")
        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 50, message = "First name should be from 2 to 50 characters")
        String firstName,

        @Schema(description = "Прізвище", example = "Іваненко")
        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 50, message = "Last name should be from 2 to 50 characters")
        String lastName
) {
    @Override
    public String toString() {
        return "SystemAdminCreateRequest[email=" + email + ", password=****, firstName=" + firstName
                + ", lastName=" + lastName + "]";
    }
}