package com.example.help_bridge.users.systemadmin.exception;

public class SystemAdminHasVerificationActsException extends RuntimeException {
    public SystemAdminHasVerificationActsException(Long id) {
        super("Cannot delete system admin " + id + ": verification acts are linked to this admin");
    }

    public SystemAdminHasVerificationActsException(Long id, Throwable cause) {
        super("Cannot delete system admin " + id + ": verification acts are linked to this admin", cause);
    }
}