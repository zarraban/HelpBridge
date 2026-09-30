package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestVerificationDto;

public interface RequestVerificationService {
    void reviewRequest(Long id, RequestVerificationDto reviewRequest);
}