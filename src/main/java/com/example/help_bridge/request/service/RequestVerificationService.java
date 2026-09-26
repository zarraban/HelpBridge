package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.request.RequestVerificationDto;

public interface RequestVerificationService {
    void reviewRequest(Long id, RequestVerificationDto reviewRequest);
}