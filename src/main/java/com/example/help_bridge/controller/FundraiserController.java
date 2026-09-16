package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.SendMailingRequest;
import com.example.help_bridge.dto.response.FundraiserResponse;
import com.example.help_bridge.dto.response.SendMailingResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

// TODO When using Thymeleaf will be converted to @Controller
@RestController
@RequestMapping("/api/fundraiser")
public class FundraiserController {

    @GetMapping("/id")
    public ResponseEntity<FundraiserResponse> getFundraiserById(){
        return ResponseEntity.ok(null);
    }

    // TODO add field which will be used to sort by
    @GetMapping
    public ResponseEntity<List<FundraiserResponse>> getAllFundraisers(
            @RequestParam(value = "sort", defaultValue = "asc") String sort,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "5") Long size
    ){
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping
    public ResponseEntity<SendMailingResponse> sendMailToDonors(
            @RequestBody @Valid SendMailingRequest request
            ){
        return ResponseEntity.ok(null);
    }
}
