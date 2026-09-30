package com.example.help_bridge.users.fundrepresentative.exception;

public class FundRepresentativeNotFoundException extends RuntimeException {
    public FundRepresentativeNotFoundException(Long id) {
        super("Fund representative not found with id: " + id);
    }
}