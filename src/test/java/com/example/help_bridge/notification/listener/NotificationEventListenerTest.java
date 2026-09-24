package com.example.help_bridge.notification.listener;

import com.example.help_bridge.donor.dto.response.DonorResponse;
import com.example.help_bridge.donor.service.DonorService;
import com.example.help_bridge.fundraiser.event.MassMailingRequestedEvent;
import com.example.help_bridge.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private DonorService donorService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    void onMassMailingRequested_shouldFetchDonorsAndScheduleEmails_whenEventReceived() {
        MassMailingRequestedEvent event = new MassMailingRequestedEvent(100L, "Subject", "Body");
        
        DonorResponse donor1 = new DonorResponse(1L, 100L, "John", "Doe", "john@example.com", "123", LocalDateTime.now());
        DonorResponse donor2 = new DonorResponse(2L, 100L, "Jane", "Doe", "jane@example.com", "456", LocalDateTime.now());
        
        when(donorService.getDonorsByFundraiserId(100L)).thenReturn(List.of(donor1, donor2));

        listener.onMassMailRequested(event);

        verify(donorService).getDonorsByFundraiserId(100L);
        verify(notificationService).scheduleEmails(List.of("john@example.com", "jane@example.com"), "Subject", "Body");
    }
}