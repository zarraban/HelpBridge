package com.example.help_bridge.fund.controller;

import com.example.help_bridge.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fund.dto.request.FundDescriptUpdateRequest;
import com.example.help_bridge.fund.dto.response.FundResponse;
import com.example.help_bridge.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fund.service.FundService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/funds")
@Validated
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
    public ResponseEntity<FundResponse> getFundById(@PathVariable(value = "id") @NotNull Long id) {
        return ResponseEntity.ok(service.getFundById(id));
    }

    @PostMapping
    public ResponseEntity<FundResponse> createFund(@RequestBody @Valid FundCreateRequest request) {
        FundResponse response = service.createFund(request);
        return ResponseEntity
                .created(URI.create("/funds/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FundResponse> updateFundStatus(
            @PathVariable(value = "id") @NotNull Long id,
            @RequestBody @Valid FundStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFundStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFundById(@PathVariable(value = "id") @NotNull Long id) {
        service.deleteFundById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<FundResponse> updateFundDescription(
            @PathVariable(value = "id") @NotNull Long id,
            @RequestBody @Valid FundDescriptUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateFundDescription(id, request));
    }
}