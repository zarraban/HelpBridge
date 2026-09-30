package com.example.help_bridge.fundraising.request.exception;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(Long id) {
        super("Request with ID " + id + " was not found");
    }
}