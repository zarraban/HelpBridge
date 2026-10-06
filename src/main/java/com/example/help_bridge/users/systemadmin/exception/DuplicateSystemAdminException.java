package com.example.help_bridge.users.systemadmin.exception;

public class DuplicateSystemAdminException extends RuntimeException {
    public DuplicateSystemAdminException(String email) {
        super("System admin with email '" + email + "' is already registered");
    }

    public DuplicateSystemAdminException(String email, Throwable cause) {
        super("System admin with email '" + email + "' is already registered", cause);
    }
}