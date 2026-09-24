package com.example.help_bridge.fundraiser.service.impl;

import com.example.help_bridge.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraiser.repository.EvidenceRepository;
import com.example.help_bridge.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraiser.strategy.EvidenceValidatorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceImplTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private FundraiserRepository fundraiserRepository;

    @Mock
    private EvidenceValidatorStrategy strategy;

    private EvidenceServiceImpl evidenceService;

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceServiceImpl(evidenceRepository, fundraiserRepository, List.of(strategy));
    }

    @Test
    void addEvidenceToFundraiser_shouldThrowInvalidEvidenceException_whenNoStrategySupports() {
        when(fundraiserRepository.findById(1L)).thenReturn(Optional.of(new Fundraiser()));
        when(strategy.supports(any())).thenReturn(false);

        assertThrows(InvalidEvidenceException.class, 
            () -> evidenceService.addEvidenceToFundraiser(1L, new AddEvidenceRequest("receipt", "feedback", "url")));
    }

    @Test
    void addEvidenceToFundraiser_shouldSave_whenStrategySupportsAndValidates() {
        when(fundraiserRepository.findById(1L)).thenReturn(Optional.of(new Fundraiser()));
        when(strategy.supports(any())).thenReturn(true);
        
        Evidence savedEvidence = new Evidence();
        savedEvidence.setId(10L);
        savedEvidence.setFundraiserId(1L);
        when(evidenceRepository.save(any())).thenReturn(savedEvidence);

        var response = evidenceService.addEvidenceToFundraiser(1L, new AddEvidenceRequest("receipt", "feedback", "url"));

        assertNotNull(response);
        assertEquals(10L, response.evidenceId());
        verify(strategy).validate(any());
        verify(evidenceRepository).save(any());
    }
}