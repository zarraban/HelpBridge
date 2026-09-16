package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.AddEvidenceRequest;
import com.example.help_bridge.dto.response.EvidenceResponse;
import com.example.help_bridge.service.EvidenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceControllerTest {

    @Mock
    private EvidenceService evidenceService;

    @InjectMocks
    private EvidenceController evidenceController;

    private final AddEvidenceRequest request = mock(AddEvidenceRequest.class);
    private final EvidenceResponse response = mock(EvidenceResponse.class);
    @Test
    void addEvidenceToFundraiser_shouldInvokeService() {
        Long fundraiserId = 1L;
        when(evidenceService.addEvidenceToFundraiser(fundraiserId, request)).thenReturn(response);

        ResponseEntity<EvidenceResponse> result = evidenceController.addEvidenceToFundraiser(fundraiserId, request);

        verify(evidenceService, times(1)).addEvidenceToFundraiser(fundraiserId, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getEvidencesByFundraiserId_shouldInvokeService() {
        Long fundraiserId = 1L;
        List<EvidenceResponse> responses = List.of(response);
        when(evidenceService.getEvidencesByFundraiserId(fundraiserId)).thenReturn(responses);

        ResponseEntity<List<EvidenceResponse>> result = evidenceController.addEvidenceToFundraiser(fundraiserId);

        verify(evidenceService, times(1)).getEvidencesByFundraiserId(fundraiserId);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
    }

    @Test
    void deleteEvidenceById_shouldInvokeService() {
        Long evidenceId = 1L;
        String response = "Deleted";
        when(evidenceService.deleteEvidenceById(evidenceId)).thenReturn(response);

        ResponseEntity<String> result = evidenceController.deleteEvidenceById(evidenceId);

        verify(evidenceService, times(1)).deleteEvidenceById(evidenceId);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}
