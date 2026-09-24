package com.example.help_bridge.donor.exception;

public class DonorNotFoundException extends RuntimeException {
    public DonorNotFoundException(String message) {
        super(message);
    }
}