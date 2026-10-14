package com.example.help_bridge.common.security.dto;

import com.example.help_bridge.users.auth.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "Account type cannot be null")
        AccountType accountType,
        @NotBlank(message = "Email cannot be blank")
        String email,
        @NotBlank(message = "Password cannot be blank")
        String password
) {
    @Override
    public String toString() {
        return "LoginRequest[accountType=" + accountType + ", email=" + email + ", password=****]";
    }
}
