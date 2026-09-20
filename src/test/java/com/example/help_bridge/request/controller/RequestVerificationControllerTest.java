package com.example.help_bridge.request.controller;

import com.example.help_bridge.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.request.service.RequestVerificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestVerificationControllerTest {

    @Mock
    private RequestVerificationService verificationService;

    @InjectMocks
    private RequestVerificationController requestVerificationController;

    @Test
    void verifyRequest_shouldInvokeService() {
        Long id = 1L;
        RequestVerificationDto dto = new RequestVerificationDto(true, "All good");

        ResponseEntity<Void> response = requestVerificationController.verifyRequest(id, dto);

        verify(verificationService, times(1)).reviewRequest(id, dto);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
