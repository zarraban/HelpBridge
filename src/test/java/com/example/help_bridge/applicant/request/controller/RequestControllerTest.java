package com.example.help_bridge.applicant.request.controller;

import com.example.help_bridge.request.controller.RequestController;
import com.example.help_bridge.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.request.entity.RequestStatus;
import com.example.help_bridge.request.service.RequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private CreateRequestRequest validRequest() {
        return new CreateRequestRequest(
                "MEDICAL",
                new BigDecimal("1500.00"),
                LocalDate.now().plusDays(10),
                "Situation description",
                "Need description",
                "Some Institution",
                "APP-001"
        );
    }

    private RequestResponse sampleResponse() {
        return new RequestResponse(
                1L, "MEDICAL", new BigDecimal("1500.00"), LocalDate.now().plusDays(10),
                "Situation description", "Need description", "Some Institution",
                "APP-001", RequestStatus.PENDING_VERIFICATION, false
        );
    }

    @Test
    void createRequest_withValidBody_returnsCreatedResponse() {
        CreateRequestRequest requestDto = validRequest();
        RequestResponse expectedResponse = sampleResponse();

        when(requestService.createRequest(any(CreateRequestRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<RequestResponse> response = requestController.createRequest(requestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(requestService).createRequest(requestDto);
    }

    @Test
    void getSharedPool_returnsList() {
        List<RequestResponse> expectedList = List.of(sampleResponse());

        when(requestService.getSharedPool()).thenReturn(expectedList);

        ResponseEntity<List<RequestResponse>> response = requestController.getSharedPool();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedList, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(requestService).getSharedPool();
    }
}