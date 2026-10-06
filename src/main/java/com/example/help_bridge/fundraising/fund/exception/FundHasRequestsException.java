package com.example.help_bridge.fundraising.fund.exception;

public class FundHasRequestsException extends RuntimeException {
    public FundHasRequestsException(Long id) {
        super("Cannot delete fund " + id + ": requests are linked to this fund");
    }
}
