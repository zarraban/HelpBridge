package com.example.help_bridge.users.fundrepresentative.exception;

public class DuplicateFundRepresentativeException extends RuntimeException {
    public DuplicateFundRepresentativeException(String email) {
        super("Fund representative with email already exists: " + email);
    }
}