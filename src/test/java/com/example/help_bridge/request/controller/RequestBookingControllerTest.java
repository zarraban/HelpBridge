package com.example.help_bridge.request.controller;

import com.example.help_bridge.request.dto.request.RequestBookingDto;
import com.example.help_bridge.request.service.RequestBookingService;
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
class RequestBookingControllerTest {

    @Mock
    private RequestBookingService requestBookingService;

    @InjectMocks
    private RequestBookingController requestBookingController;

    @Test
    void bookRequest_shouldInvokeService() {
        Long id = 1L;
        Long fundId = 2L;
        RequestBookingDto dto = new RequestBookingDto(fundId);

        ResponseEntity<Void> response = requestBookingController.bookRequest(id, dto);

        verify(requestBookingService, times(1)).bookRequest(id, fundId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
