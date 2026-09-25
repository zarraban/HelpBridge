package com.example.help_bridge.notification.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MailingTask {
    private Long id;
    
    private List<String> targetEmails;
    
    private String subject;
    private String messageBody;
    
    private MailingStatus status;
    private LocalDateTime createdAt;
}
