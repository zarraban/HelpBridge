package com.example.help_bridge.fundraising.request.exception;

public class InvalidAssistanceTypeException extends RuntimeException {

    public InvalidAssistanceTypeException(String code) {
        super("Невідомий тип допомоги: " + code);
    }
}