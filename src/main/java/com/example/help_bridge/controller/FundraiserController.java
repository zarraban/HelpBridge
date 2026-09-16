package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.SendMailingRequest;
import com.example.help_bridge.dto.response.FundraiserResponse;
import com.example.help_bridge.dto.response.SendMailingResponse;
import com.example.help_bridge.service.FundraiserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // TODO add field which will be used to sort by
    @GetMapping
    public ResponseEntity<List<FundraiserResponse>> getAllFundraisers(
            @RequestParam(value = "sort", defaultValue = "asc") String sort,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "5") Long size
    ){
        return ResponseEntity.ok(fundraiserService.getAllFundraisers(sort, page, size));
    }

    @PostMapping("/mail")
    public ResponseEntity<SendMailingResponse> sendMailToDonors(
            @RequestBody @Valid SendMailingRequest request
            ){
        return ResponseEntity.ok(fundraiserService.sendMailToDonors(request));
    }
}
