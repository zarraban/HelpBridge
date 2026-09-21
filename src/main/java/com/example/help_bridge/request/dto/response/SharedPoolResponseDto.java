package com.example.help_bridge.request.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SharedPoolResponseDto(
        Long id,
        String assistanceType,
        BigDecimal amount,
        LocalDate deadline,
        String situationDescription,
        String needDescription,
        String institutionName,
        String applicationNumber,
        boolean isHot
) {}
