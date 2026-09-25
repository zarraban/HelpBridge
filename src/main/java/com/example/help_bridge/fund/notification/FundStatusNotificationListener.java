package com.example.help_bridge.fund.notification;

import com.example.help_bridge.fund.event.FundStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class FundStatusNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(FundStatusNotificationListener.class);

    @EventListener
    @Async
    void on(FundStatusChangedEvent event) {
        log.info("Async notification: fund [" + event.fundId() + "] status changed {} -> {}",
                 event.previousStatus(), event.newStatus());
    }
}
