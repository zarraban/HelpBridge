package com.example.help_bridge.users.volunteer.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterVolunteerForm(
        @NotBlank(message = "Вкажіть ім’я")
        @Size(max = 50, message = "Ім’я має містити до 50 символів")
        String firstName,
        @NotBlank(message = "Вкажіть прізвище")
        @Size(max = 50, message = "Прізвище має містити до 50 символів")
        String lastName,
        @NotBlank(message = "Вкажіть email")
        @Email(message = "Вкажіть коректний email")
        @Size(max = 254, message = "Email має містити до 254 символів")
        String email,
        @NotBlank(message = "Вкажіть телефон")
        @Pattern(regexp = "^\\+380\\d{9}$", message = "Вкажіть телефон у форматі +380XXXXXXXXX")
        String phone,
        @NotBlank(message = "Вкажіть пароль")
        @Size(min = 8, max = 72, message = "Пароль має містити від 8 до 72 символів")
        String password
) {
    @Override
    public String toString() {
        return "RegisterVolunteerForm[firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + ", phone=" + phone + ", password=****]";
    }
}
