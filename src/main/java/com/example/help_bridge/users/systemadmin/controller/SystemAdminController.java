package com.example.help_bridge.users.systemadmin.controller;

import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminUpdateRequest;
import com.example.help_bridge.users.systemadmin.dto.response.SystemAdminResponse;
import com.example.help_bridge.users.systemadmin.service.SystemAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/system-admins")
public class SystemAdminController {

    private final SystemAdminService service;

    public SystemAdminController(SystemAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SystemAdminResponse>> getAllSystemAdmins() {
        return ResponseEntity.ok(service.getAllSystemAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemAdminResponse> getSystemAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSystemAdminById(id));
    }

    @PostMapping
    public ResponseEntity<SystemAdminResponse> createSystemAdmin(@RequestBody @Valid SystemAdminCreateRequest request) {
        SystemAdminResponse response = service.createSystemAdmin(request);
        return ResponseEntity
                .created(URI.create("/system-admins/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SystemAdminResponse> updateSystemAdmin(
            @PathVariable Long id,
            @RequestBody @Valid SystemAdminUpdateRequest request
    ) {
        return ResponseEntity.ok(service.updateSystemAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSystemAdminById(@PathVariable Long id) {
        service.deleteSystemAdminById(id);
        return ResponseEntity.noContent().build();
    }
}