package com.example.help_bridge.fund.strategy;

import com.example.help_bridge.fund.entity.Fund;
import com.example.help_bridge.fund.entity.FundStatus;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ApprovedTransitionHandler implements  FundStatusTransitionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApprovedTransitionHandler.class);

    @Override
    public boolean supports(FundStatus targetStatus){
        return targetStatus == FundStatus.APPROVED;
    }

    @Override
    public void handle(Fund fund){
        log.info("Fund " + fund.getFundName() + " (" + fund.getId() + " )" + " has been approved and can start receiving donations");
    }
}
