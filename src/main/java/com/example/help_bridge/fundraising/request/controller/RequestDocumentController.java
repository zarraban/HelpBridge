package com.example.help_bridge.fundraising.request.controller;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.CreateDocumentRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import com.example.help_bridge.fundraising.request.service.RequestDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/requests/{requestId}/documents")
public class RequestDocumentController {

    private final RequestDocumentService documentService;

    public RequestDocumentController(RequestDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> addDocument(
            @PathVariable Long requestId,
            @RequestBody @Valid CreateDocumentRequest request) {
        DocumentResponse created = documentService.addDocument(requestId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable Long requestId) {
        return ResponseEntity.ok(documentService.getDocuments(requestId));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long requestId, @PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getDocument(requestId, documentId));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long requestId,
            @PathVariable Long documentId,
            @RequestBody @Valid CreateDocumentRequest request) {
        return ResponseEntity.ok(documentService.updateDocument(requestId, documentId, request));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long requestId, @PathVariable Long documentId) {
        documentService.deleteDocument(requestId, documentId);
        return ResponseEntity.noContent().build();
    }
}