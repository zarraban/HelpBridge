package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;

import java.util.List;

public interface RequestService {
    RequestResponse createRequest(CreateRequestRequest request);
    List<RequestResponse> getSharedPool();
}