package com.example.help_bridge.request.controller;

import com.example.help_bridge.request.dto.RequestDto.CreateRequestRequest;
import com.example.help_bridge.request.dto.RequestDto.RequestResponse;
import com.example.help_bridge.request.service.RequestService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createRequest_shouldInvokeService() {
        CreateRequestRequest request = mock(CreateRequestRequest.class);
        RequestResponse response = mock(RequestResponse.class);
        when(response.id()).thenReturn(1L);
        when(requestService.createRequest(request)).thenReturn(response);

        ResponseEntity<RequestResponse> result = requestController.createRequest(request);

        verify(requestService, times(1)).createRequest(request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getSharedPool_shouldInvokeService() {
        List<RequestResponse> pool = List.of(mock(RequestResponse.class));
        when(requestService.getSharedPool()).thenReturn(pool);

        ResponseEntity<List<RequestResponse>> result = requestController.getSharedPool();

        verify(requestService, times(1)).getSharedPool();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(pool, result.getBody());
    }
}
