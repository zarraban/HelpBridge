package com.example.help_bridge.fundraiser.service;

import com.example.help_bridge.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraiser.dto.response.FundraiserResponse;
import com.example.help_bridge.fundraiser.dto.response.SendMailingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundraiserService {

    public FundraiserResponse getFundraiserById(Long id) {
        return null;
    }

    public List<FundraiserResponse> getAllFundraisers(String sort, Long page, Long size) {
        return null;
    }

    public SendMailingResponse sendMailToDonors(SendMailingRequest request) {
        return null;
    }
}
