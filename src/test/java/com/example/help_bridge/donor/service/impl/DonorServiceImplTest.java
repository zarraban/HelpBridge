package com.example.help_bridge.donor.service.impl;

import com.example.help_bridge.donor.exception.DonorNotFoundException;
import com.example.help_bridge.donor.dto.request.DonorRequest;
import com.example.help_bridge.donor.dto.response.DonorResponse;
import com.example.help_bridge.donor.entity.Donor;
import com.example.help_bridge.donor.repository.DonorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceImplTest {

    @Mock
    private DonorRepository donorRepository;

    @InjectMocks
    private DonorServiceImpl donorService;

    @Test
    void getDonorById_shouldThrowDonorNotFoundException_whenDonorDoesNotExist() {
        when(donorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DonorNotFoundException.class, () -> donorService.getDonorById(1L));
    }

    @Test
    void getDonorById_shouldReturnDonorResponse_whenDonorExists() {
        Donor donor = new Donor();
        donor.setId(1L);
        donor.setFirstName("John");
        donor.setEmail("test@example.com");

        when(donorRepository.findById(1L)).thenReturn(Optional.of(donor));

        DonorResponse response = donorService.getDonorById(1L);

        assertNotNull(response);
        assertEquals("John", response.firstName());
        assertEquals("test@example.com", response.email());
    }

    @Test
    void addNewDonor_shouldSaveAndReturnResponse_whenCalled() {
        DonorRequest request = new DonorRequest(100L, "Jane", "Doe", "jane@example.com", "123456");
        
        Donor savedDonor = new Donor();
        savedDonor.setId(2L);
        savedDonor.setFundraiserId(100L);
        savedDonor.setFirstName("Jane");
        savedDonor.setEmail("jane@example.com");
        savedDonor.setCreatedAt(LocalDateTime.now());

        when(donorRepository.save(any(Donor.class))).thenReturn(savedDonor);

        DonorResponse response = donorService.addNewDonor(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("jane@example.com", response.email());
        verify(donorRepository).save(any(Donor.class));
    }
    
    @Test
    void getDonorsByFundraiserId_shouldReturnList_whenDonorsExist() {
        Donor donor = new Donor();
        donor.setId(1L);
        donor.setFundraiserId(100L);
        
        when(donorRepository.findByFundraiserId(100L)).thenReturn(List.of(donor));
        
        List<DonorResponse> responses = donorService.getDonorsByFundraiserId(100L);
        
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(100L, responses.getFirst().fundraiserId());
    }
    
    @Test
    void updateDonorFields_shouldThrowDonorNotFoundException_whenDonorDoesNotExist() {
        DonorRequest request = new DonorRequest(100L, "Jane", "Doe", "jane@example.com", "123456");
        when(donorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DonorNotFoundException.class, () -> donorService.updateDonorFields(1L, request));
    }
}