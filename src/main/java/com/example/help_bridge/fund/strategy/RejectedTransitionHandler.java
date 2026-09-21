package com.example.help_bridge.fund.strategy;

import com.example.help_bridge.fund.entity.Fund;
import com.example.help_bridge.fund.entity.FundStatus;
import org.springframework.stereotype.Component;

@Component
public class RejectedTransitionHandler implements FundStatusTransitionHandler {

    @Override
    public boolean supports(FundStatus targetStatus){
        return targetStatus == FundStatus.REJECTED;
    }

    @Override
    public void handle(Fund fund){

    }
}
