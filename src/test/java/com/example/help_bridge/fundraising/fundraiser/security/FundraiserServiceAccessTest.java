package com.example.help_bridge.fundraising.fundraiser.security;

import com.example.help_bridge.fundraising.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.repository.*;
import com.example.help_bridge.fundraising.fundraiser.service.impl.*;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundraiserServiceAccessTest {
    @Mock RequestAccessPolicy identity;
    @Mock com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy volunteers;
    @Mock FundraiserAssignmentRepository assignments;
    @Mock RequestRepository requests;
    @Mock FundraiserRepository fundraisers;
    @Mock EvidenceRepository evidences;
    @Mock ApplicationEventPublisher events;
    FundraiserServiceImpl service;
    EvidenceServiceImpl evidenceService;
    Fundraiser fundraiser;

    @BeforeEach
    void setUp() {
        var access = new FundraiserAccessPolicy(identity, volunteers, assignments, requests);
        service = new FundraiserServiceImpl(fundraisers, events, access);
        evidenceService = new EvidenceServiceImpl(evidences, fundraisers, List.of(), access);
        fundraiser = new Fundraiser();
        fundraiser.setId(1L);
        fundraiser.setRequestId(10L);
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        when(identity.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("FUND_REPRESENTATIVE", null, 2L));
    }

    @Test
    void foreignDetailsEvidenceCloseAndMailAreDeniedWithoutSideEffects() {
        when(fundraisers.findById(1L)).thenReturn(Optional.of(fundraiser));
        var evidence = new Evidence();
        evidence.setFundraiser(fundraiser);
        when(evidences.findById(3L)).thenReturn(Optional.of(evidence));
        assertThrows(AccessDeniedException.class, () -> service.getFundraiserById(1L));
        assertThrows(AccessDeniedException.class, () -> service.closeFundraiser(1L));
        assertThrows(AccessDeniedException.class, () -> service.sendMailToDonors(new SendMailingRequest(1L, "Subject", "Body")));
        assertThrows(AccessDeniedException.class, () -> evidenceService.getEvidencesByFundraiserId(1L));
        assertThrows(AccessDeniedException.class, () -> evidenceService.deleteEvidenceById(3L));
        verify(fundraisers, never()).save(any());
        verify(evidences, never()).delete(any());
        verify(evidences, never()).findAllByFundraiserIdOrderByCreatedAtAsc(any());
        verifyNoInteractions(events);
        assertEquals(FundraiserStatus.IN_PROGRESS, fundraiser.getStatus());
    }

    @Test
    void representativeListUsesFundScopedQuery() {
        when(fundraisers.findAllByFundId(eq(2L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(fundraiser)));
        assertEquals(1, service.getAllFundraisers("asc", 1L, 5L).size());
        verify(fundraisers, never()).findAll();
    }

    @Test
    void adminListUsesAllFundraisers() {
        when(identity.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("SYSTEM_ADMIN", null, null));
        when(fundraisers.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(fundraiser)));
        assertEquals(1, service.getAllFundraisers("asc", 1L, 5L).size());
        verify(fundraisers, never()).findAllByFundId(any(), any());
    }

    @Test
    void ownFundraiserCanBeClosedAndEvidenceReadAndDeleted() {
        when(fundraisers.findById(1L)).thenReturn(Optional.of(fundraiser));
        when(requests.existsByIdAndFundId(10L, 2L)).thenReturn(true);
        var evidence = new Evidence();
        evidence.setFundraiser(fundraiser);
        fundraiser.setEvidences(List.of(evidence));
        when(evidences.findById(3L)).thenReturn(Optional.of(evidence));
        when(evidences.findAllByFundraiserIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(evidence));
        assertEquals(1, evidenceService.getEvidencesByFundraiserId(1L).size());
        evidenceService.deleteEvidenceById(3L);
        verify(evidences).delete(evidence);
        service.closeFundraiser(1L);
        assertEquals(FundraiserStatus.CLOSED, fundraiser.getStatus());
    }
}
