package com.example.help_bridge.fundraising.fundraiser.service;

import com.example.help_bridge.fundraising.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.response.EvidenceResponse;
import java.util.List;

public interface EvidenceService {
    EvidenceResponse addEvidenceToFundraiser(Long fundraiserId, AddEvidenceRequest request);
    List<EvidenceResponse> getEvidencesByFundraiserId(Long fundraiserId);
    String deleteEvidenceById(Long evidenceId);
}
