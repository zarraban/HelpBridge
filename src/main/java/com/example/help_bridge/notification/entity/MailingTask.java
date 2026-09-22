package com.example.help_bridge.notification.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MailingTask {
    private Long id;
    
    // Store emails as a comma-separated string
    private String targetEmails; 
    
    private String subject;
    private String messageBody;
    
    private MailingStatus status;
    private LocalDateTime createdAt;
}
