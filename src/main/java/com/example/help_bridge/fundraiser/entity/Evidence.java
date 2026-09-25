package com.example.help_bridge.fundraiser.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Evidence {
    private Long id;
    private Long fundraiserId;
    private String receiptNumber;
    private String recipientFeedback;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}
