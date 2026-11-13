package com.example.help_bridge.fundraising.fundraiser.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VolunteerFundraiserResponse(
        FundraiserResponse fundraiser,
        RequestSummary request,
        List<FundraiserAssignmentResponse> assignments
) {
    public record RequestSummary(String assistanceType, BigDecimal amount, LocalDate deadline,
                                 String institutionName, String needDescription) {}
}
