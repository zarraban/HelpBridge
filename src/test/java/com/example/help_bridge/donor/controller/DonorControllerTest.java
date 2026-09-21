package com.example.help_bridge.donor.controller;

import com.example.help_bridge.donor.dto.request.DonorRequest;
import com.example.help_bridge.donor.dto.response.DonorResponse;
import com.example.help_bridge.donor.service.DonorService;
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
class DonorControllerTest {

    @Mock
    private DonorService donorService;

    @InjectMocks
    private DonorController donorController;

    private final DonorRequest request = mock(DonorRequest.class);
    private final DonorResponse response = mock(DonorResponse.class);

    @Test
    void addNewDonor_shouldInvokeService() {
        when(donorService.addNewDonor(request)).thenReturn(response);

        ResponseEntity<DonorResponse> result = donorController.addNewDonor(request);

        verify(donorService, times(1)).addNewDonor(request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getDonorById_shouldInvokeService() {
        Long id = 1L;
        when(donorService.getDonorById(id)).thenReturn(response);

        ResponseEntity<DonorResponse> result = donorController.getDonorById(id);

        verify(donorService, times(1)).getDonorById(id);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getAllDonors_shouldInvokeService() {
        String sort = "desc";
        Long page = 2L;
        Long size = 10L;
        List<DonorResponse> responses = List.of(response);
        when(donorService.getAllDonors(sort, page, size)).thenReturn(responses);

        ResponseEntity<List<DonorResponse>> result = donorController.getAllDonors(sort, page, size);

        verify(donorService, times(1)).getAllDonors(sort, page, size);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
    }

    @Test
    void updateDonorFields_shouldInvokeService() {
        Long id = 1L;
        when(donorService.updateDonorFields(id, request)).thenReturn(response);

        ResponseEntity<DonorResponse> result = donorController.updateDonorFields(id, request);

        verify(donorService, times(1)).updateDonorFields(id, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteDonorById_shouldInvokeService() {
        Long id = 1L;
        when(donorService.deleteDonorById(id)).thenReturn(response);

        ResponseEntity<DonorResponse> result = donorController.deleteDonorById(id);

        verify(donorService, times(1)).deleteDonorById(id);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}
