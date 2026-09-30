package com.example.help_bridge.fundraising.request.service.impl;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.CreateDocumentRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestDocument;
import com.example.help_bridge.fundraising.request.exception.RequestDocumentNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestDocumentRepository;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.service.RequestDocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestDocumentServiceImplTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestDocumentRepository documentRepository;

    private RequestDocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentService = new RequestDocumentServiceImpl(requestRepository, documentRepository);
    }

    private Request newRequest(Long id) {
        Request request = new Request(null, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", "inst", "A-1", true);
        ReflectionTestUtils.setField(request, "id", id);
        return request;
    }

    private RequestDocument newDocument(Long id) {
        RequestDocument document = new RequestDocument("a.pdf", "http://x/a.pdf");
        ReflectionTestUtils.setField(document, "id", id);
        return document;
    }

    @Test
    void addDocument_attachesDocumentToRequest() {
        Request request = newRequest(1L);
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));
        when(documentRepository.save(any(RequestDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentResponse response = documentService.addDocument(1L,
                new CreateDocumentRequest("a.pdf", "http://x/a.pdf"));

        assertThat(response.fileName()).isEqualTo("a.pdf");
        assertThat(request.getDocuments()).hasSize(1);
    }

    @Test
    void addDocument_whenRequestMissing_throwsNotFound() {
        when(requestRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.addDocument(99L,
                new CreateDocumentRequest("a.pdf", "http://x/a.pdf")))
                .isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    void getDocument_fromAnotherRequest_throwsNotFound() {
        Request other = newRequest(2L);
        RequestDocument document = newDocument(10L);
        other.addDocument(document);
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> documentService.getDocument(1L, 10L))
                .isInstanceOf(RequestDocumentNotFoundException.class);
    }

    @Test
    void updateDocument_changesFields() {
        Request request = newRequest(1L);
        RequestDocument document = newDocument(10L);
        request.addDocument(document);
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        DocumentResponse response = documentService.updateDocument(1L, 10L,
                new CreateDocumentRequest("b.pdf", "http://x/b.pdf"));

        assertThat(response.fileName()).isEqualTo("b.pdf");
        assertThat(response.fileUrl()).isEqualTo("http://x/b.pdf");
    }

    @Test
    void deleteDocument_removesDocumentFromRequest() {
        Request request = newRequest(1L);
        request.addDocument(newDocument(10L));
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));

        documentService.deleteDocument(1L, 10L);

        assertThat(request.getDocuments()).isEmpty();
    }

    @Test
    void deleteDocument_whenDocumentMissing_throwsNotFound() {
        Request request = newRequest(1L);
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> documentService.deleteDocument(1L, 10L))
                .isInstanceOf(RequestDocumentNotFoundException.class);
    }
}