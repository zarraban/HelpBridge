package com.example.help_bridge.fundraising.fund.dto.response;

import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeFundResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Інформація про фонд")
public record FundResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Фонд Допомоги Дітям") String fundName,
        @Schema(example = "12345678") String edrpou,
        @Schema(example = "+380501234567") String phoneNumber,
        @Schema(example = "UA213223130000026007233566001") String bankDetail,
        @Schema(example = "м. Київ, вул. Хрещатик, 1") String registeredAddress,
        @Schema(example = "м. Київ, вул. Хрещатик, 1") String actualAddress,
        @Schema(example = "contact@fund-help.org") String corpEmail,
        @Schema(example = "https://fund-help.org") String website,
        @Schema(example = "{\"facebook\": \"https://facebook.com/fundhelp\"}") Map<String, String> socialMediaUrls,
        @Schema(example = "Допомагаємо дітям, які залишились без піклування") String description,
        @Schema(description = "Поточний статус фонду") FundStatus status,
        @Schema(description = "Представники фонду") List<FundRepresentativeFundResponse> representatives
) {
}