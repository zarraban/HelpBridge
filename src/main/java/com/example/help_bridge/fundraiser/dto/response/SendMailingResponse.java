package com.example.help_bridge.fundraiser.dto.response;

import com.example.help_bridge.fundraiser.entity.MailingStatus;

import java.time.LocalDateTime;

public record SendMailingResponse(
        Long fundraiserId,
        int recipientsCount,
        MailingStatus status,
        LocalDateTime sentAt
) {
}
