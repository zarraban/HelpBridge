package com.example.help_bridge.notification.service.impl;

import com.example.help_bridge.notification.entity.MailingStatus;
import com.example.help_bridge.notification.entity.MailingTask;
import com.example.help_bridge.notification.repository.MailingTaskRepository;
import com.example.help_bridge.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private MailingTaskRepository repository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void scheduleEmails_shouldSaveMailingTask_whenInvoked() {
        //given
        List<String> testEmailToSend = List.of("test@gmail.com");
        String subject = "Donation Request";
        String text = "We need your help";

        //when
        notificationService.scheduleEmails(testEmailToSend,subject,text);
        //then

        ArgumentCaptor<MailingTask> captor = ArgumentCaptor.forClass(MailingTask.class);
        verify(repository).save(captor.capture());

        MailingTask savedTask = captor.getValue();
        assertNotNull(savedTask);
        assertEquals(testEmailToSend, savedTask.getTargetEmails());
        assertEquals(subject, savedTask.getSubject());
        assertEquals(text, savedTask.getMessageBody());
        assertEquals(MailingStatus.QUEUED, savedTask.getStatus());
        assertNotNull(savedTask.getCreatedAt());
    }
}