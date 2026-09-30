package com.example.help_bridge.fundraising.request.controller;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.service.RequestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setRequestURI("/api/v1/requests");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private CreateRequestRequest validRequest() {
        return new CreateRequestRequest(
                null,
                "MEDICAL",
                new BigDecimal("1500.00"),
                LocalDate.now().plusDays(10),
                "Situation description",
                "Need description",
                "Some Institution",
                "APP-001",
                true
        );
    }

    private RequestResponse sampleResponse() {
        return new RequestResponse(
                1L, "MEDICAL", new BigDecimal("1500.00"), LocalDate.now().plusDays(10),
                "Situation description", "Need description", "Some Institution",
                "APP-001", RequestStatus.PENDING_VERIFICATION, false,
                LocalDateTime.now(), List.of()
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

    @Test
    void getAllRequests_withoutStatus_returnsAll() {
        List<RequestResponse> expectedList = List.of(sampleResponse());
        when(requestService.getAllRequests(null)).thenReturn(expectedList);

        ResponseEntity<List<RequestResponse>> response = requestController.getAllRequests(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedList, response.getBody());
    }

    @Test
    void searchByInstitution_returnsMatches() {
        List<RequestResponse> expectedList = List.of(sampleResponse());
        when(requestService.searchByInstitution("hospital")).thenReturn(expectedList);

        ResponseEntity<List<RequestResponse>> response = requestController.searchByInstitution("hospital");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedList, response.getBody());
    }

    @Test
    void getRequestsByUser_returnsUserRequests() {
        List<RequestResponse> expectedList = List.of(sampleResponse());
        when(requestService.getRequestsByUser(7L)).thenReturn(expectedList);

        ResponseEntity<List<RequestResponse>> response = requestController.getRequestsByUser(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedList, response.getBody());
    }

    @Test
    void getRequestById_returnsRequest() {
        RequestResponse expectedResponse = sampleResponse();

        when(requestService.getRequestById(1L)).thenReturn(expectedResponse);

        ResponseEntity<RequestResponse> response = requestController.getRequestById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void updateRequest_returnsUpdatedRequest() {
        CreateRequestRequest requestDto = validRequest();
        RequestResponse expectedResponse = sampleResponse();

        when(requestService.updateRequest(1L, requestDto)).thenReturn(expectedResponse);

        ResponseEntity<RequestResponse> response = requestController.updateRequest(1L, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void deleteRequest_returnsNoContent() {
        ResponseEntity<Void> response = requestController.deleteRequest(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(requestService).deleteRequest(1L);
    }
}