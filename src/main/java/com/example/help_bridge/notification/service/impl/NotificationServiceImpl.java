package com.example.help_bridge.notification.service.impl;

import com.example.help_bridge.notification.entity.MailingStatus;
import com.example.help_bridge.notification.entity.MailingTask;
import com.example.help_bridge.notification.repository.MailingTaskRepository;
import com.example.help_bridge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MailingTaskRepository mailingTaskRepository;

    @Override
    public void scheduleEmails(List<String> emails, String subject, String text) {
        MailingTask task = new MailingTask();
        task.setTargetEmails(emails);
        task.setSubject(subject);
        task.setMessageBody(text);
        task.setStatus(MailingStatus.QUEUED);
        task.setCreatedAt(LocalDateTime.now());
        
        mailingTaskRepository.save(task);
    }
}