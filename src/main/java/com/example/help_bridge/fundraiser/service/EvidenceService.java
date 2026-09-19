package com.example.help_bridge.fundraiser.service;

import com.example.help_bridge.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraiser.dto.response.EvidenceResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvidenceService {

    public EvidenceResponse addEvidenceToFundraiser(Long fundraiserId, AddEvidenceRequest request) {
        return null;
    }

    public List<EvidenceResponse> getEvidencesByFundraiserId(Long fundraiserId) {
        return null;
    }

    public String deleteEvidenceById(Long evidenceId) {
        return null;
    }
}
