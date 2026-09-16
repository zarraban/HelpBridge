package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.SendMailingRequest;
import com.example.help_bridge.dto.response.FundraiserResponse;
import com.example.help_bridge.dto.response.SendMailingResponse;
import com.example.help_bridge.service.FundraiserService;
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
class FundraiserControllerTest {

    @Mock
    private FundraiserService fundraiserService;

    @InjectMocks
    private FundraiserController fundraiserController;

    private final FundraiserResponse response = mock(FundraiserResponse.class);
    @Test
    void getFundraiserById_shouldInvokeService() {
        Long id = 1L;
        FundraiserResponse response = mock(FundraiserResponse.class);
        when(fundraiserService.getFundraiserById(id)).thenReturn(response);

        ResponseEntity<FundraiserResponse> result = fundraiserController.getFundraiserById(id);

        verify(fundraiserService, times(1)).getFundraiserById(id);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getAllFundraisers_shouldInvokeService() {
        String sort = "asc";
        Long page = 1L;
        Long size = 5L;
        List<FundraiserResponse> responses = List.of(response);
        when(fundraiserService.getAllFundraisers(sort, page, size)).thenReturn(responses);

        ResponseEntity<List<FundraiserResponse>> result = fundraiserController.getAllFundraisers(sort, page, size);

        verify(fundraiserService, times(1)).getAllFundraisers(sort, page, size);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
    }

    @Test
    void sendMailToDonors_shouldInvokeService() {
        SendMailingRequest request = mock(SendMailingRequest.class);
        SendMailingResponse response = mock(SendMailingResponse.class);
        when(fundraiserService.sendMailToDonors(request)).thenReturn(response);

        ResponseEntity<SendMailingResponse> result = fundraiserController.sendMailToDonors(request);

        verify(fundraiserService, times(1)).sendMailToDonors(request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}
