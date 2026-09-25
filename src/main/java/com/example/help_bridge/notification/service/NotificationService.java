package com.example.help_bridge.notification.service;

import java.util.List;

public interface NotificationService {
    void scheduleEmails(List<String> emails, String subject, String text);
}
