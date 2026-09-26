package com.example.help_bridge.applicant.request.controller;

import com.example.help_bridge.request.controller.RequestVerificationController;
import com.example.help_bridge.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.request.exception.RequestNotFoundException;
import com.example.help_bridge.request.service.RequestVerificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestVerificationControllerTest {

    @Mock
    private RequestVerificationService verificationService;

    @InjectMocks
    private RequestVerificationController requestVerificationController;

    @Test
    void verifyRequest_withApprovedTrue_returns204() {
        RequestVerificationDto dto = new RequestVerificationDto(true, "All good");
        doNothing().when(verificationService).reviewRequest(1L, dto);

        ResponseEntity<Void> response = requestVerificationController.verifyRequest(1L, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(verificationService).reviewRequest(1L, dto);
    }

    @Test
    void verifyRequest_whenRequestNotFound_throwsException() {
        RequestVerificationDto dto = new RequestVerificationDto(false, "Not valid");
        doThrow(new RequestNotFoundException(99L))
                .when(verificationService).reviewRequest(99L, dto);

        assertThrows(RequestNotFoundException.class,
                () -> requestVerificationController.verifyRequest(99L, dto));

        verify(verificationService).reviewRequest(99L, dto);
    }
}