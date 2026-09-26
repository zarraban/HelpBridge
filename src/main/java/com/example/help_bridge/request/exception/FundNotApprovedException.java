package com.example.help_bridge.request.exception;

public class FundNotApprovedException extends RuntimeException {
    public FundNotApprovedException(Long fundId) {
        super("Fund with ID " + fundId + " does not have APPROVED status and cannot book requests");
    }
}