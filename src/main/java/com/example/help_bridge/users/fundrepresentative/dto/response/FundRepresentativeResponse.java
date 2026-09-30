package com.example.help_bridge.users.fundrepresentative.dto.response;

import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;

public record FundRepresentativeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Long fundId
) {
    public static FundRepresentativeResponse from(FundRepresentative r) {
        return new FundRepresentativeResponse(
                r.getId(),
                r.getFirstName(),
                r.getLastName(),
                r.getEmail(),
                r.getPhone(),
                r.getFundId()
        );
    }
}