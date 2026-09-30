package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;

import java.util.List;

public interface RequestService {
    RequestResponse createRequest(CreateRequestRequest request);
    List<RequestResponse> getSharedPool();
}