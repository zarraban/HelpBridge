package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.UpdateRequestRequest;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;

import java.util.List;

public interface RequestService {
    RequestResponse createRequest(CreateRequestRequest request);
    List<RequestResponse> getAllRequests(RequestStatus status);
    List<RequestResponse> getSharedPool();
    List<RequestResponse> searchByInstitution(String institution);
    List<RequestResponse> getRequestsByUser(Long userId);
    RequestResponse getRequestById(Long id);
    RequestResponse updateRequest(Long id, UpdateRequestRequest request);
    void deleteRequest(Long id);
}