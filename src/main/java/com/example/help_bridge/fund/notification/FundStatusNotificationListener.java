package com.example.help_bridge.fund.notification;

import com.example.help_bridge.fund.event.FundStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class FundStatusNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(FundStatusNotificationListener.class);

    @ApplicationModuleListener
    void on(FundStatusChangedEvent event) {
        log.info("Async notification: fund [{}] status changed {} -> {}",
                event.fundId(), event.previousStatus(), event.newStatus());
    }
}
