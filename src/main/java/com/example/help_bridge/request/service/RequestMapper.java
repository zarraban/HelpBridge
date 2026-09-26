package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.request.entity.Request;

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