package com.example.help_bridge.users.systemadmin.dto.response;

public record SystemAdminResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}