package com.example.help_bridge.users.auth;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public enum AccountType {
    SYSTEM_ADMIN,
    FUND_REPRESENTATIVE,
    VOLUNTEER;

    public static AccountType fromString(String type){
        try{
            return valueOf(type.toUpperCase().trim());
        }catch (IllegalArgumentException | NullPointerException e){
            throw new UsernameNotFoundException("Unknown account type: " + type);
        }
    }
}
