package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;

public interface RequestBookingService {
    RequestResponse bookRequest(Long requestId, Long fundId);
}