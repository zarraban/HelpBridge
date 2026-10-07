package com.example.help_bridge.users.systemadmin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemAdminCreateRequest(
        @NotBlank(message = "This field is necessary")
        @Email(message = "Incorrect email format")
        @Size(max = 100, message = "Email is too long")
        String email,

        @NotBlank(message = "This field is necessary")
        @Size(min = 8, max = 72, message = "Password should be from 8 to 72 characters")
        String password,

        @NotBlank(message = "This field is necessary")
        @Size(min = 2, max = 50, message = "First name should be from 2 to 50 characters")
        String firstName,

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