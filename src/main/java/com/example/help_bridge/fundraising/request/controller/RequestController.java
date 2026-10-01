package com.example.help_bridge.fundraising.request.controller;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.service.RequestService;
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

    /** Усі запити; за потреби фільтр ?status=NEW */
    @GetMapping
    public ResponseEntity<List<RequestResponse>> getAllRequests(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(requestService.getAllRequests(status));
    }

    @GetMapping("/shared-pool")
    public ResponseEntity<List<RequestResponse>> getSharedPool() {
        return ResponseEntity.ok(requestService.getSharedPool());
    }

    /** Пошук за частиною назви закладу: /search?institution=лікарня */
    @GetMapping("/search")
    public ResponseEntity<List<RequestResponse>> searchByInstitution(@RequestParam String institution) {
        return ResponseEntity.ok(requestService.searchByInstitution(institution));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<RequestResponse>> getRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(requestService.getRequestsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponse> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestResponse> updateRequest(
            @PathVariable Long id,
            @RequestBody @Valid CreateRequestRequest request) {
        return ResponseEntity.ok(requestService.updateRequest(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}