package com.example.help_bridge.fundraising.fund.strategy;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;

public interface FundStatusTransitionHandler {
    boolean supports(FundStatus targetStatus);
    void handle(Fund fund);
}
