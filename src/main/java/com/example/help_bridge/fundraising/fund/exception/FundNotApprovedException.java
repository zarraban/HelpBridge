package com.example.help_bridge.fundraising.fund.exception;

public class FundNotApprovedException extends RuntimeException {
    public FundNotApprovedException(Long id) {
        super("Fund with id " + id + " is not approved yet, description can not be changed");
    }
}