package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;

public interface RequestBookingService {
    RequestResponse bookRequest(Long requestId, Long fundId);
}