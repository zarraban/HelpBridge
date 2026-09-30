package com.example.help_bridge.fundraising.fund.service;

import com.example.help_bridge.fundraising.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fundraising.fund.dto.response.FundResponse;
import com.example.help_bridge.fundraising.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundDescriptUpdateRequest;
import java.util.List;

public interface FundService {
    List<FundResponse> getAllFunds();
    FundResponse getFundById(Long id);
    FundResponse createFund(FundCreateRequest request);
    FundResponse updateFundStatus(Long id, FundStatusUpdateRequest request);
    FundResponse updateFundDescription(Long id, FundDescriptUpdateRequest request);
    void deleteFundById(Long id);
}