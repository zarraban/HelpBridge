package com.example.help_bridge.users.systemadmin.exception;

public class SystemAdminNotFoundException extends RuntimeException {
    public SystemAdminNotFoundException(Long id) {
        super("Can not find system admin with id " + id);
    }
}