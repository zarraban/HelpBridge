package com.example.help_bridge.fundraising.request.exception;

public class RequesterNotFoundException extends RuntimeException {
    public RequesterNotFoundException(Long userId) {
        super("User with ID " + userId + " was not found");
    }
}