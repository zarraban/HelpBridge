package com.example.help_bridge.request.controller;

import com.example.help_bridge.request.dto.RequestDto.CreateRequestRequest;
import com.example.help_bridge.request.dto.RequestDto.RequestResponse;
import com.example.help_bridge.request.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<RequestResponse> createRequest(@RequestBody @Valid CreateRequestRequest request) {
        RequestResponse created = requestService.createRequest(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/shared-pool")
    public ResponseEntity<List<RequestResponse>> getSharedPool() {
        List<RequestResponse> pool = requestService.getSharedPool();
        return ResponseEntity.ok(pool);
    }
}