package com.example.help_bridge.fundraising.fundraiser.service.impl;

import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.fundraising.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.repository.EvidenceRepository;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraising.fundraiser.strategy.EvidenceValidatorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(1L);
        Evidence savedEvidence = new Evidence();
        savedEvidence.setId(10L);
        savedEvidence.setFundraiser(fundraiser);
        when(evidenceRepository.save(any())).thenReturn(savedEvidence);

        var response = evidenceService.addEvidenceToFundraiser(1L, new AddEvidenceRequest("receipt", "feedback", "url"));

        assertNotNull(response);
        assertEquals(10L, response.evidenceId());
        assertEquals(1L, response.fundraiserId());
        verify(strategy).validate(any());
        verify(evidenceRepository).save(any());
    }

    @Test
    void addEvidenceToFundraiser_shouldLinkEvidenceToFoundFundraiser() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(1L);
        when(fundraiserRepository.findById(1L)).thenReturn(Optional.of(fundraiser));
        when(strategy.supports(any())).thenReturn(true);
        when(evidenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        evidenceService.addEvidenceToFundraiser(1L, new AddEvidenceRequest("receipt", "feedback", "url"));

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        verify(evidenceRepository).save(captor.capture());
        assertSame(fundraiser, captor.getValue().getFundraiser());
        assertNotNull(captor.getValue().getCreatedAt());
    }

    @Test
    void addEvidenceToFundraiser_shouldThrowFundraiserNotFoundException_whenFundraiserDoesNotExist() {
        when(fundraiserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FundraiserNotFoundException.class,
            () -> evidenceService.addEvidenceToFundraiser(1L, new AddEvidenceRequest("receipt", "feedback", "url")));

        verify(evidenceRepository, never()).save(any());
    }

    @Test
    void getEvidencesByFundraiserId_shouldMapEvidencesOfFundraiser() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(1L);
        Evidence evidence = new Evidence();
        evidence.setId(10L);
        evidence.setFundraiser(fundraiser);
        when(evidenceRepository.findAllByFundraiserIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(evidence));

        var responses = evidenceService.getEvidencesByFundraiserId(1L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.getFirst().evidenceId());
        assertEquals(1L, responses.getFirst().fundraiserId());
    }
}
