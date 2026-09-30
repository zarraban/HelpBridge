package com.example.help_bridge.users.fundrepresentative.service;

import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeRequest;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeUpdateRequest;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeResponse;

import java.util.List;

public interface FundRepresentativeService {
    FundRepresentativeResponse create(FundRepresentativeRequest request);
    FundRepresentativeResponse getById(Long id);
    List<FundRepresentativeResponse> getAll();
    List<FundRepresentativeResponse> getByFundId(Long fundId);
    FundRepresentativeResponse update(Long id, FundRepresentativeUpdateRequest request);
    void delete(Long id);
}