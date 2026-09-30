package com.example.help_bridge.users.fundrepresentative.dto.response;

import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;

public record FundRepresentativeFundResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone
) {
    public static FundRepresentativeFundResponse from(FundRepresentative r) {
        return new FundRepresentativeFundResponse(
                r.getId(), r.getFirstName(), r.getLastName(), r.getEmail(), r.getPhone());
    }
}