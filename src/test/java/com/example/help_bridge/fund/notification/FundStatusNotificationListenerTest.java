package com.example.help_bridge.fund.notification;

import com.example.help_bridge.fund.entity.FundStatus;
import com.example.help_bridge.fund.event.FundStatusChangedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FundStatusNotificationListenerTest {

    private final FundStatusNotificationListener listener = new FundStatusNotificationListener();

    @Test
    void onHandlesEventWithoutThrowing() {
        FundStatusChangedEvent event = new FundStatusChangedEvent(1L, FundStatus.PENDING_APPROVAL, FundStatus.APPROVED);

        assertDoesNotThrow(() -> listener.on(event));
    }
}