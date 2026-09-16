package com.example.help_bridge.controller;

import com.example.help_bridge.dto.VolunteerRequest;
import com.example.help_bridge.dto.VolunteerResponse;
import com.example.help_bridge.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/funds/{fundId}/volunteers")
public class VolunteerController {

    private final VolunteerService service;

    public VolunteerController(VolunteerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VolunteerResponse>> getAllFundVolunteers(@PathVariable UUID fundId) {
        return ResponseEntity.ok(service.getFundVolunteers(fundId));
    }

    @GetMapping("/{volunteerId}")
    public ResponseEntity<VolunteerResponse> getFundVolunteerById(
            @PathVariable UUID fundId,
            @PathVariable UUID volunteerId
    ) {
        return ResponseEntity.ok(service.getFundVolunteer(fundId, volunteerId));
    }

    @PostMapping
    public ResponseEntity<VolunteerResponse> addVolunteer(
            @PathVariable UUID fundId,
            @RequestBody @Valid VolunteerRequest request
    ) {
        VolunteerResponse created = service.addVolunteer(fundId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{volunteerId}")
    public ResponseEntity<VolunteerResponse> updateVolunteer(
            @PathVariable UUID fundId,
            @PathVariable UUID volunteerId,
            @RequestBody @Valid VolunteerRequest request
    ) {
        return ResponseEntity.ok(service.updateVolunteer(fundId, volunteerId, request));
    }

    @DeleteMapping("/{volunteerId}")
    public ResponseEntity<Void> removeVolunteer(
            @PathVariable UUID fundId,
            @PathVariable UUID volunteerId
    ) {
        service.removeVolunteer(fundId, volunteerId);
        return ResponseEntity.noContent().build();
    }
}
