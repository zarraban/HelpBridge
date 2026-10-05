package com.example.help_bridge.fundraising.fund.controller;

import com.example.help_bridge.fundraising.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundDescriptUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.response.FundResponse;
import com.example.help_bridge.fundraising.fund.service.FundService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.example.help_bridge.fundraising.fund.dto.request.FundUpdateRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/funds")
public class FundController {

    private final FundService service;

    public FundController(FundService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<FundResponse>> getAllFunds() {
        return ResponseEntity.ok(service.getAllFunds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FundResponse> getFundById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getFundById(id));
    }

    @PostMapping
    public ResponseEntity<FundResponse> createFund(@RequestBody @Valid FundCreateRequest request) {
        FundResponse response = service.createFund(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FundResponse> updateFund(
            @PathVariable("id") Long id,
            @RequestBody @Valid FundUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFund(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FundResponse> updateFundStatus(
            @PathVariable("id") Long id,
            @RequestBody @Valid FundStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFundStatus(id, request));
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<FundResponse> updateFundDescription(
            @PathVariable("id") Long id,
            @RequestBody @Valid FundDescriptUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFundDescription(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFundById(@PathVariable("id") Long id) {
        service.deleteFundById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{fundId}/representatives/{representativeId}")
    public ResponseEntity<Void> removeRepresentative(@PathVariable("fundId") Long fundId,
                                                     @PathVariable("representativeId") Long representativeId) {
        service.removeRepresentative(fundId, representativeId);
        return ResponseEntity.noContent().build();
    }


}