package com.example.help_bridge.fund.exception;

public class DuplicateFundException extends RuntimeException {
    public DuplicateFundException(String edrpou) {
        super("Fund with EDRPOU '" + edrpou + "' is already registered");
    }
}
