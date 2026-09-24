package com.example.help_bridge.notification.listener;

import com.example.help_bridge.donor.service.DonorService;
import com.example.help_bridge.fundraiser.event.MassMailingRequestedEvent;
import com.example.help_bridge.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationEventListener {

    private final DonorService donorService;
    private final NotificationService notificationService;

    public NotificationEventListener(DonorService donorService,
                                     NotificationService notificationService) {
        this.donorService = donorService;
        this.notificationService = notificationService;
    }

//    @ApplicationModuleListener
    @EventListener
    @Async
    public void onMassMailRequested(MassMailingRequestedEvent event) {
        System.out.println("MassMailingRequestedEvent handling started");
        List<String> emailToSent = donorService.getDonorsByFundraiserId(event.fundraiserId())
                .stream().map(donor -> donor.email())
                .toList();

        notificationService.scheduleEmails(emailToSent,
                event.subject(),
                event.message());

        System.out.println("MassMailingRequestedEvent handling finished");

    }
}
