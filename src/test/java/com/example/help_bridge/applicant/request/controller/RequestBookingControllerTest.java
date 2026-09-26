package com.example.help_bridge.applicant.request.controller;

import com.example.help_bridge.request.controller.RequestBookingController;
import com.example.help_bridge.request.dto.request.RequestBookingDto;
import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.request.entity.RequestStatus;
import com.example.help_bridge.request.exception.FundNotApprovedException;
import com.example.help_bridge.request.exception.InvalidRequestStateException;
import com.example.help_bridge.request.service.RequestBookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestBookingControllerTest {

    @Mock
    private RequestBookingService requestBookingService;

    @InjectMocks
    private RequestBookingController requestBookingController;

    private RequestResponse sampleResponse(RequestStatus status) {
        return new RequestResponse(
                1L, "MEDICAL", new BigDecimal("1500.00"), LocalDate.now().plusDays(10),
                "Situation description", "Need description", "Some Institution",
                "APP-001", status, false
        );
    }

    @Test
    void bookRequest_withValidBody_returnsAccepted() {
        RequestBookingDto dto = new RequestBookingDto(2L);
        RequestResponse expectedResponse = sampleResponse(RequestStatus.IN_PROGRESS);

        when(requestBookingService.bookRequest(1L, 2L)).thenReturn(expectedResponse);

        ResponseEntity<RequestResponse> response = requestBookingController.bookRequest(1L, dto);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(requestBookingService).bookRequest(1L, 2L);
    }

    @Test
    void bookRequest_whenRequestAlreadyInProgress_throwsException() {
        RequestBookingDto dto = new RequestBookingDto(2L);

        when(requestBookingService.bookRequest(1L, 2L))
                .thenThrow(new InvalidRequestStateException("Request with ID 1 is already being processed or closed"));

        assertThrows(InvalidRequestStateException.class,
                () -> requestBookingController.bookRequest(1L, dto));

        verify(requestBookingService).bookRequest(1L, dto.fundId());
    }

    @Test
    void bookRequest_whenFundNotApproved_throwsException() {
        RequestBookingDto dto = new RequestBookingDto(2L);

        when(requestBookingService.bookRequest(1L, 2L))
                .thenThrow(new FundNotApprovedException(2L));

        assertThrows(FundNotApprovedException.class,
                () -> requestBookingController.bookRequest(1L, dto));

        verify(requestBookingService).bookRequest(1L, dto.fundId());
    }
}