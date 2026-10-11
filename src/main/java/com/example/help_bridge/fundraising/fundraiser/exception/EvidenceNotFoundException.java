package com.example.help_bridge.fundraising.fundraiser.exception;

public class EvidenceNotFoundException extends RuntimeException {
    public EvidenceNotFoundException(Long id) {
        super("Evidence with ID " + id + " not found");
    }
}
