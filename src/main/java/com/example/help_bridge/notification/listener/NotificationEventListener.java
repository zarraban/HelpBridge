package com.example.help_bridge.notification.listener;

import com.example.help_bridge.users.donor.service.DonorService;
import com.example.help_bridge.fundraising.fundraiser.event.MassMailingRequestedEvent;
import com.example.help_bridge.notification.service.NotificationService;
import com.example.help_bridge.users.volunteer.event.VolunteerRegisteredEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerRemovedEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedEmailEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedPhoneNumberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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

    @EventListener
    @Async
    public void onVolunteerRegistered(VolunteerRegisteredEvent event) {
        System.out.println("VolunteerRegisteredEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Р’С–С‚Р°С”РјРѕ Сѓ HelpBridge!",
                ("Р’С–С‚Р°С”РјРѕ, " + event.firstName() + "! Р’Р°СЃ Р·Р°СЂРµС”СЃС‚СЂРѕРІР°РЅРѕ Сѓ С„РѕРЅРґС– в„–" + event.fundId() + "!"));

        System.out.println("VolunteerRegisteredEvent handling finished");
    }

    @EventListener
    @Async
    public void onVolunteerRemoved(VolunteerRemovedEvent event) {
        System.out.println("VolunteerRemovedEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Р‘СѓРІР°Р№С‚Рµ!",
                ("Р’С–С‚Р°С”РјРѕ, " + event.firstName() + ". Р’Р°С€ Р°РєР°СѓРЅС‚ РґРµР°РєС‚РёРІРѕРІР°РЅРѕ."));

        System.out.println("VolunteerRemovedEvent handling finished");
    }

    @EventListener
    @Async
    public void onVolunteerUpdatedEmail(VolunteerUpdatedEmailEvent event) {
        System.out.println("VolunteerUpdatedEmailEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Р—РјС–РЅР° РїРѕС€С‚Рё Сѓ HelpBridge!",
                ("Р’С–С‚Р°С”РјРѕ, " + event.firstName() + "! РџРµСЂРµРІС–СЂСЏС”РјРѕ РІР°С€Сѓ РЅРѕРІСѓ РїРѕС€С‚Сѓ С†РёРј Р»РёСЃС‚РѕРј!"));

        System.out.println("VolunteerUpdatedEmailEvent handling finished");
    }

    @EventListener
    @Async
    public void onVolunteerUpdatedPhoneNumber(VolunteerUpdatedPhoneNumberEvent event) {
        System.out.println("VolunteerUpdatedPhoneNumberEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Р—РјС–РЅР° РїРѕС€С‚Рё Сѓ HelpBridge!",
                ("Р’С–С‚Р°С”РјРѕ, " + event.firstName() + "! РџРµСЂРµРІС–СЂСЏС”РјРѕ РІР°С€Сѓ РЅРѕРІСѓ РїРѕС€С‚Сѓ С†РёРј Р»РёСЃС‚РѕРј!"));

        System.out.println("VolunteerUpdatedPhoneNumberEvent handling finished");
    }
}
