package com.example.help_bridge.fundraising.fund.dto.response;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import java.util.Map;

public record FundResponse(
        Long id,
        String fundRepresName,
        String fundRepresSurname,
        String fundName,
        String edrpou,
        String phoneNumber,
        String bankDetail,
        String registeredAddress,
        String actualAddress,
        String corpEmail,
        String website,
        Map<String, String> socialMediaUrls,
        String description,
        FundStatus status
) {
}
