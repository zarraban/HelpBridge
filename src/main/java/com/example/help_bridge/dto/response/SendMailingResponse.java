package com.example.help_bridge.dto.response;

import com.example.help_bridge.entity.enums.MailingStatus;

import java.time.LocalDateTime;

public record SendMailingResponse(
        Long fundraiserId,
        int recipientsCount,
        MailingStatus status,
        LocalDateTime sentAt
) {
}
