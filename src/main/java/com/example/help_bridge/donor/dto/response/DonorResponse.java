package com.example.help_bridge.donor.dto.response;

import java.time.LocalDateTime;

public record DonorResponse(
        Long id,
        Long fundraiserId,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDateTime createdAt
) {
}
