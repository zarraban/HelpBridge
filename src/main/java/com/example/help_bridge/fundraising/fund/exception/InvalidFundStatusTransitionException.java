package com.example.help_bridge.fundraising.fund.exception;

import com.example.help_bridge.fundraising.fund.entity.FundStatus;

public class InvalidFundStatusTransitionException extends RuntimeException {
    public InvalidFundStatusTransitionException(FundStatus from, FundStatus to) {
        super("Inadmissible status transition from " + from + " to " + to);
    }
}
