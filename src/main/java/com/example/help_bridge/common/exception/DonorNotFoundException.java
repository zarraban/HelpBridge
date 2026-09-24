package com.example.help_bridge.common.exception;

public class DonorNotFoundException extends RuntimeException {
    public DonorNotFoundException(String message) {
        super(message);
    }
}