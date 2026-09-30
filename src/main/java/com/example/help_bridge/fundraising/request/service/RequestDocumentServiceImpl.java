package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.CreateDocumentRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestDocument;
import com.example.help_bridge.fundraising.request.exception.RequestDocumentNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestDocumentRepository;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RequestDocumentServiceImpl implements RequestDocumentService {

    private final RequestRepository requestRepository;
    private final RequestDocumentRepository documentRepository;

    public RequestDocumentServiceImpl(RequestRepository requestRepository,
                                      RequestDocumentRepository documentRepository) {
        this.requestRepository = requestRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public DocumentResponse addDocument(Long requestId, CreateDocumentRequest dto) {
        Request request = requestRepository.findByIdWithDocuments(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        RequestDocument document = new RequestDocument(dto.fileName(), dto.fileUrl());
        request.addDocument(document);
        documentRepository.saveAndFlush(document);
        return toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long requestId) {
        if (!requestRepository.existsById(requestId)) {
            throw new RequestNotFoundException(requestId);
        }
        return documentRepository.findByRequestId(requestId).stream()
                .map(RequestDocumentServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long requestId, Long documentId) {
        return toResponse(findDocument(requestId, documentId));
    }

    @Override
    public DocumentResponse updateDocument(Long requestId, Long documentId, CreateDocumentRequest dto) {
        RequestDocument document = findDocument(requestId, documentId);
        document.update(dto.fileName(), dto.fileUrl());
        return toResponse(document);
    }

    @Override
    public void deleteDocument(Long requestId, Long documentId) {
        Request request = requestRepository.findByIdWithDocuments(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        RequestDocument document = request.getDocuments().stream()
                .filter(d -> documentId.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new RequestDocumentNotFoundException(documentId));

        request.removeDocument(document);
    }

    private RequestDocument findDocument(Long requestId, Long documentId) {
        if (!requestRepository.existsById(requestId)) {
            throw new RequestNotFoundException(requestId);
        }
        return documentRepository.findByIdAndRequestId(documentId, requestId)
                .orElseThrow(() -> new RequestDocumentNotFoundException(documentId));
    }

    private static DocumentResponse toResponse(RequestDocument d) {
        return new DocumentResponse(d.getId(), d.getFileName(), d.getFileUrl());
    }
}