package com.example.help_bridge.donor.dto.response;

import java.time.LocalDateTime;

public record DonorResponse(
        Long donorId,
        String fullName,
        String email,
        String phone,
        LocalDateTime createdAt
) {
}
