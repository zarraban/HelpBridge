package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.AddEvidenceRequest;
import com.example.help_bridge.dto.response.EvidenceResponse;
import com.example.help_bridge.service.EvidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService){
        this.evidenceService = evidenceService;
    }

    // TODO volunteer can't add evidences to other fundraisers except for his own
    @PostMapping("/fundraisers/{fundraiserId}/evidences")
    public ResponseEntity<EvidenceResponse> addEvidenceToFundraiser(
            @PathVariable Long fundraiserId,
            @RequestBody @Valid AddEvidenceRequest evidenceRequest
    ) {
        return ResponseEntity.ok(evidenceService.addEvidenceToFundraiser(fundraiserId, evidenceRequest));
    }

    @GetMapping("/fundraisers/{fundraiserId}/evidences")
    public ResponseEntity<List<EvidenceResponse>> addEvidenceToFundraiser(
            @PathVariable Long fundraiserId
    ) {
        return ResponseEntity.ok(evidenceService.getEvidencesByFundraiserId(fundraiserId));
    }

    // only fund representative can delete evidences(even if they contain mistakes)
    @DeleteMapping("/evidences/{id}")
    public ResponseEntity<String> deleteEvidenceById(
            @PathVariable("id") Long evidenceId
    ) {
        return ResponseEntity.ok(evidenceService.deleteEvidenceById(evidenceId));
    }

}
