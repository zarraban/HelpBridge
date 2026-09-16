package com.example.help_bridge.service;

import com.example.help_bridge.dto.request.SendMailingRequest;
import com.example.help_bridge.dto.response.FundraiserResponse;
import com.example.help_bridge.dto.response.SendMailingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundraiserService {

    public FundraiserResponse getFundraiserById() {
        return null;
    }

    public List<FundraiserResponse> getAllFundraisers(String sort, Long page, Long size) {
        return null;
    }

    public SendMailingResponse sendMailToDonors(SendMailingRequest request) {
        return null;
    }
}
