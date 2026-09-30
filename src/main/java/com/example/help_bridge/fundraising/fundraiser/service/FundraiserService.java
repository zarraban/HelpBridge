package com.example.help_bridge.fundraising.fundraiser.service;

import com.example.help_bridge.fundraising.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.SendMailingResponse;
import java.util.List;

public interface FundraiserService {
    FundraiserResponse getFundraiserById(Long id);
    List<FundraiserResponse> getAllFundraisers(String sort, Long page, Long size);
    SendMailingResponse sendMailToDonors(SendMailingRequest request);
    void closeFundraiser(Long id);
}