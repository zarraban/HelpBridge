package com.example.help_bridge.fundraising.fund.exception;

public class FundNotFoundException extends RuntimeException{
    public FundNotFoundException(Long id) {
        super("Can not find fund with id " + id);
    }
}
