package com.example.help_bridge.fundraising.fundraiser.controller;

import com.example.help_bridge.fundraising.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.SendMailingResponse;
import com.example.help_bridge.fundraising.fundraiser.service.FundraiserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO When using Thymeleaf will be converted to @Controller
@RestController
@RequestMapping("/api/fundraiser")
public class FundraiserController {

    private final FundraiserService fundraiserService;

    public FundraiserController(FundraiserService fundraiserService){
        this.fundraiserService = fundraiserService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<FundraiserResponse> getFundraiserById(@PathVariable("id") Long id){
        return ResponseEntity.ok(fundraiserService.getFundraiserById(id));
    }

    @GetMapping
    public ResponseEntity<List<FundraiserResponse>> getAllFundraisers(
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sort", defaultValue = "asc") String sort,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "5") Long size
    ){
        var result = fundraiserService.getFundraisers(sortBy, sort, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", Long.toString(result.getTotalElements()))
                .header("X-Total-Pages", Integer.toString(result.getTotalPages()))
                .header("X-Page", Long.toString(page))
                .header("X-Page-Size", Long.toString(size))
                .body(result.getContent());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'FUND_REPRESENTATIVE')")
    @PostMapping("/mail")
    public ResponseEntity<SendMailingResponse> sendMailToDonors(
            @RequestBody @Valid SendMailingRequest request
            ){
        return ResponseEntity.ok(fundraiserService.sendMailToDonors(request));
    }
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'FUND_REPRESENTATIVE')")
    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeFundraiser(@PathVariable("id") Long id) {
        fundraiserService.closeFundraiser(id);
        return ResponseEntity.noContent().build();
    }
}