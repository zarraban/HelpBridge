package com.example.help_bridge.common.security.dto;

public record TokenResponse(String token, String tokenType, long expiresInSeconds) {
}
