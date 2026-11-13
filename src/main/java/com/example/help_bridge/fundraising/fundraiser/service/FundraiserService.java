package com.example.help_bridge.fundraising.fundraiser.service;

import org.springframework.data.domain.Page;

import com.example.help_bridge.fundraising.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.SendMailingResponse;
import java.util.List;

public interface FundraiserService {
    FundraiserResponse getFundraiserById(Long id);
    default List<FundraiserResponse> getAllFundraisers(String sort, Long page, Long size) {
        return getFundraisers("createdAt", sort, page, size).getContent();
    }
    Page<FundraiserResponse> getFundraisers(String sortBy, String sort, Long page, Long size);
    SendMailingResponse sendMailToDonors(SendMailingRequest request);
    void closeFundraiser(Long id);
}