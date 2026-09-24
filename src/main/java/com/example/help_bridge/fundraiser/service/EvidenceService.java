package com.example.help_bridge.fundraiser.service;

import com.example.help_bridge.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraiser.dto.response.EvidenceResponse;
import java.util.List;

public interface EvidenceService {
    EvidenceResponse addEvidenceToFundraiser(Long fundraiserId, AddEvidenceRequest request);
    List<EvidenceResponse> getEvidencesByFundraiserId(Long fundraiserId);
    String deleteEvidenceById(Long evidenceId);
}
