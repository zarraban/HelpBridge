package com.example.help_bridge.fundraising.fund.strategy;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApprovedTransitionHandler implements FundStatusTransitionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApprovedTransitionHandler.class);

    @Override
    public boolean supports(FundStatus targetStatus) {
        return targetStatus == FundStatus.APPROVED;
    }

    @Override
    public void handle(Fund fund) {
        log.info("Fund '{}' (id={}) has been approved and can start receiving donations",
                fund.getFundName(), fund.getId());
    }
}