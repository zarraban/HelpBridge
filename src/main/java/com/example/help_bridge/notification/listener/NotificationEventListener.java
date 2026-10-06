package com.example.help_bridge.notification.listener;

import com.example.help_bridge.users.donor.dto.response.DonorResponse;
import com.example.help_bridge.users.donor.service.DonorService;
import com.example.help_bridge.fundraising.fundraiser.event.MassMailingRequestedEvent;
import com.example.help_bridge.notification.service.NotificationService;
import com.example.help_bridge.users.volunteer.event.VolunteerRegisteredEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerRemovedEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedEmailEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedPhoneNumberEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

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
        log.info("MassMailingRequestedEvent handling started");
        List<String> emailToSent = donorService.getDonorsByFundraiserId(event.fundraiserId())
                .stream().map(DonorResponse::email)
                .toList();

        notificationService.scheduleEmails(emailToSent,
                event.subject(),
                event.message());

        log.info("MassMailingRequestedEvent handling finished");

    }

    @EventListener
    @Async
    public void onVolunteerRegistered(VolunteerRegisteredEvent event) {
        log.info("VolunteerRegisteredEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Вітаємо у HelpBridge!",
                ("Вітаємо, " + event.firstName() + "! Вас зареєстровано у фонді №" + event.fundId() + "!"));

        log.info("VolunteerRegisteredEvent handling finished");
    }

    @EventListener
    @Async
    public void onVolunteerRemoved(VolunteerRemovedEvent event) {
        log.info("VolunteerRemovedEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Бувайте!",
                ("Вітаємо, " + event.firstName() + ". Ваш акаунт деактивовано."));

        log.info("VolunteerRemovedEvent handling finished");
    }

    @EventListener
    @Async
    public void onVolunteerUpdatedEmail(VolunteerUpdatedEmailEvent event) {
        log.info("VolunteerUpdatedEmailEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Зміна пошти у HelpBridge!",
                ("Вітаємо, " + event.firstName() + "! Перевіряємо вашу нову пошту цим листом!"));

        log.info("VolunteerUpdatedEmailEvent handling finished");
    }

    @EventListener
    @Async
    public void onVolunteerUpdatedPhoneNumber(VolunteerUpdatedPhoneNumberEvent event) {
        log.info("VolunteerUpdatedPhoneNumberEvent handling started");
        List<String> emailToSent = Arrays.asList(event.email());
        notificationService.scheduleEmails(emailToSent,
                "Зміна пошти у HelpBridge!",
                ("Вітаємо, " + event.firstName() + "! Перевіряємо вашу нову пошту цим листом!"));

        log.info("VolunteerUpdatedPhoneNumberEvent handling finished");
    }
}
