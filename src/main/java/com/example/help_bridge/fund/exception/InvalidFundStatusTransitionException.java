package com.example.help_bridge.fund.exception;

import com.example.help_bridge.fund.entity.FundStatus;

public class InvalidFundStatusTransitionException extends RuntimeException {
    public InvalidFundStatusTransitionException(FundStatus from, FundStatus to) {
        super("Inadmissible status transition from " + from + " to " + to);
    }
}
