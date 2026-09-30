package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.CreateDocumentRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;

import java.util.List;

public interface RequestDocumentService {
    DocumentResponse addDocument(Long requestId, CreateDocumentRequest dto);
    List<DocumentResponse> getDocuments(Long requestId);
    DocumentResponse getDocument(Long requestId, Long documentId);
    DocumentResponse updateDocument(Long requestId, Long documentId, CreateDocumentRequest dto);
    void deleteDocument(Long requestId, Long documentId);
}