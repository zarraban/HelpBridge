package com.example.help_bridge.fundraiser.dto.response;

import java.time.LocalDateTime;

public record SendMailingResponse(
        Long fundraiserId,
        int recipientsCount,
        LocalDateTime sentAt
) {
}
