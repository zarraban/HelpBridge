package com.example.help_bridge.fund.strategy;

import com.example.help_bridge.fund.entity.Fund;
import com.example.help_bridge.fund.entity.FundStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RejectedTransitionHandler implements FundStatusTransitionHandler {

    private static final Logger log = LoggerFactory.getLogger(RejectedTransitionHandler.class);

    @Override
    public boolean supports(FundStatus targetStatus){
        return targetStatus == FundStatus.REJECTED;
    }

    @Override
    public void handle(Fund fund){
        log.info("Fund " + fund.getFundName() + " (" + fund.getId() + " )" + " has been rejected");
    }
}
