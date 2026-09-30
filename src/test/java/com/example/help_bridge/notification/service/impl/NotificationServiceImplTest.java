package com.example.help_bridge.notification.service.impl;

import com.example.help_bridge.notification.entity.MailingStatus;
import com.example.help_bridge.notification.entity.MailingTask;
import com.example.help_bridge.notification.repository.MailingTaskJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private MailingTaskJpaRepository MailingTaskJpaRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void scheduleEmails_shouldSaveMailingTask_whenInvoked() {
        List<String> testEmailToSend = List.of("test@gmail.com");
        String subject = "Donation Request";
        String text = "We need your help";

        notificationService.scheduleEmails(testEmailToSend, subject, text);

        ArgumentCaptor<MailingTask> captor = ArgumentCaptor.forClass(MailingTask.class);
        verify(MailingTaskJpaRepository).save(captor.capture());

        MailingTask savedTask = captor.getValue();
        assertNotNull(savedTask);
        assertEquals(1, savedTask.getRecipients().size());
        assertEquals("test@gmail.com", savedTask.getRecipients().get(0).getEmail());
        assertEquals(subject, savedTask.getSubject());
        assertEquals(text, savedTask.getMessageBody());
        assertEquals(MailingStatus.PENDING, savedTask.getStatus());
        assertNotNull(savedTask.getCreatedAt());
    }
}