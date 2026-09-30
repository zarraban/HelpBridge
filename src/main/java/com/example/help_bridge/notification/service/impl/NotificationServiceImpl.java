package com.example.help_bridge.notification.service.impl;

import com.example.help_bridge.notification.entity.MailingRecipient;
import com.example.help_bridge.notification.entity.MailingStatus;
import com.example.help_bridge.notification.entity.MailingTask;
import com.example.help_bridge.notification.entity.RecipientStatus;
import com.example.help_bridge.notification.repository.MailingTaskJpaRepository;
import com.example.help_bridge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MailingTaskJpaRepository MailingTaskJpaRepository;

    @Override
    public void scheduleEmails(List<String> emails, String subject, String text) {
        MailingTask task = new MailingTask();
        task.setSubject(subject);
        task.setMessageBody(text);
        task.setStatus(MailingStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        
        if (emails != null) {
            for (String email : emails) {
                MailingRecipient r = new MailingRecipient();
                r.setEmail(email);
                r.setStatus(RecipientStatus.PENDING);
                task.addRecipient(r);
            }
        }
        
        MailingTaskJpaRepository.save(task);
    }
}