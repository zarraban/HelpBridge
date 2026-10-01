package com.example.help_bridge.fundraising.request.exception;

public class RequestDocumentNotFoundException extends RuntimeException {
    public RequestDocumentNotFoundException(Long id) {
        super("Request document " + id + " not found");
    }
}