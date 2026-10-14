package com.example.help_bridge.users.volunteer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterVolunteerRequest(
        @NotBlank(message = "First name cannot be blank")
        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName,
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Enter correct email address")
        @Size(max = 254, message = "Email must be at most 254 characters")
        String email,
        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(regexp = "^\\+380\\d{9}$", message = "Phone number must match +380XXXXXXXXX")
        String phone,
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, max = 72, message = "Password should be from 8 to 72 characters")
        String password
) {
    @Override
    public String toString() {
        return "RegisterVolunteerRequest[firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + ", phone=" + phone + ", password=****]";
    }
}
