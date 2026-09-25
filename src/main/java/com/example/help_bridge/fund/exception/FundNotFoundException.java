package com.example.help_bridge.fund.exception;

public class FundNotFoundException extends RuntimeException{
    public FundNotFoundException(Long id) {
        super("Can not find fund with id " + id);
    }
}
