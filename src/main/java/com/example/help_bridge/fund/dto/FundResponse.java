package com.example.help_bridge.fund.dto;
import com.example.help_bridge.fund.entity.FundStatus;
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
