package com.example.help_bridge.fund.strategy;

import com.example.help_bridge.fund.entity.Fund;
import com.example.help_bridge.fund.entity.FundStatus;

public interface FundStatusTransitionHandler {
    boolean supports(FundStatus targetStatus);
    void handle(Fund fund);
}
