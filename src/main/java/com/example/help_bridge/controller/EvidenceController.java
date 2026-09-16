package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.AddEvidenceRequest;
import com.example.help_bridge.dto.response.EvidenceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class EvidenceController {


    // TODO volunteer can't add evidences to other fundraisers except for his own
    @PostMapping("/fundraisers/{fundraiserId}/evidences")
    public ResponseEntity<EvidenceResponse> addEvidenceToFundraiser(
            @PathVariable Long fundraiserId,
            @RequestBody @Valid AddEvidenceRequest evidenceRequest
    ) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/fundraisers/{fundraiserId}/evidences")
    public ResponseEntity<List<EvidenceResponse>> addEvidenceToFundraiser(
            @PathVariable Long fundraiserId
    ) {
        return ResponseEntity.ok(Collections.emptyList());
    }

    // only fund representative can delete evidences(even if they contain mistakes)
    @DeleteMapping("/evidences/evidences/{id}")
    public ResponseEntity<String> deleteEvidenceById(
            @PathVariable Long fundraiserId
    ) {
        return ResponseEntity.ok(null);
    }


}
