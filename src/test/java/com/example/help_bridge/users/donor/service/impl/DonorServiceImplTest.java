package com.example.help_bridge.users.donor.service.impl;

import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.users.donor.exception.DonorNotFoundException;
import com.example.help_bridge.users.donor.dto.request.DonorRequest;
import com.example.help_bridge.users.donor.dto.response.DonorResponse;
import com.example.help_bridge.users.donor.entity.Donor;
import com.example.help_bridge.users.donor.repository.DonorRepository;
import jakarta.persistence.EntityManager;
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

    private static final Long FUNDRAISER_ID = 100L;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private DonorServiceImpl donorService;

    private static Fundraiser fundraiser() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(FUNDRAISER_ID);
        return fundraiser;
    }

    @Test
    void getDonorById_shouldThrowDonorNotFoundException_whenDonorDoesNotExist() {
        when(donorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DonorNotFoundException.class, () -> donorService.getDonorById(1L));
    }

    @Test
    void getDonorById_shouldReturnDonorResponse_whenDonorExists() {
        Donor donor = new Donor();
        donor.setId(1L);
        donor.setFundraiser(fundraiser());
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
        DonorRequest request = new DonorRequest(FUNDRAISER_ID, "Jane", "Doe", "jane@example.com", "123456");
        when(entityManager.find(Fundraiser.class, FUNDRAISER_ID)).thenReturn(fundraiser());

        Donor savedDonor = new Donor();
        savedDonor.setId(2L);
        savedDonor.setFundraiser(fundraiser());
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
        donor.setFundraiser(fundraiser());
        
        when(donorRepository.findAllByFundraiserIdWithFundraiser(FUNDRAISER_ID)).thenReturn(List.of(donor));
        
        List<DonorResponse> responses = donorService.getDonorsByFundraiserId(FUNDRAISER_ID);
        
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(FUNDRAISER_ID, responses.getFirst().fundraiserId());
    }
    
    @Test
    void updateDonorFields_shouldThrowDonorNotFoundException_whenDonorDoesNotExist() {
        DonorRequest request = new DonorRequest(FUNDRAISER_ID, "Jane", "Doe", "jane@example.com", "123456");
        when(donorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DonorNotFoundException.class, () -> donorService.updateDonorFields(1L, request));
    }

    @Test
    void addNewDonor_shouldThrowFundraiserNotFoundException_whenFundraiserDoesNotExist() {
        DonorRequest request = new DonorRequest(FUNDRAISER_ID, "Jane", "Doe", "jane@example.com", "123456");
        when(entityManager.find(Fundraiser.class, FUNDRAISER_ID)).thenReturn(null);

        assertThrows(FundraiserNotFoundException.class, () -> donorService.addNewDonor(request));

        verify(donorRepository, never()).save(any());
    }
}
