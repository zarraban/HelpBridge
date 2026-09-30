package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.Request;

final class RequestMapper {

    private RequestMapper() {}

    static RequestResponse toResponse(Request request) {
        return new RequestResponse(
                request.getId(),
                request.getAssistanceType(),
                request.getAmount(),
                request.getDeadline(),
                request.getSituationDescription(),
                request.getNeedDescription(),
                request.getInstitutionName(),
                request.getApplicationNumber(),
                request.getStatus(),
                request.isExpired()
        );
    }
}