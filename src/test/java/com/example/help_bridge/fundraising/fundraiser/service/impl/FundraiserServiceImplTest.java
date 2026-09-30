package com.example.help_bridge.fundraising.fundraiser.service.impl;

import com.example.help_bridge.fundraising.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraising.fundraiser.event.MassMailingRequestedEvent;
import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundraiserServiceImplTest {

    @Mock
    private FundraiserJpaRepository fundraiserJpaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FundraiserServiceImpl fundraiserService;

    @Test
    void closeFundraiser_shouldThrowIllegalStateException_whenNoEvidenceAttached() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        fundraiser.setEvidences(List.of());

        when(fundraiserJpaRepository.findById(1L)).thenReturn(Optional.of(fundraiser));

        assertThrows(IllegalStateException.class, () -> fundraiserService.closeFundraiser(1L));
    }

    @Test
    void closeFundraiser_shouldUpdateStatus_whenEvidenceExists() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        fundraiser.setEvidences(List.of(new Evidence()));

        when(fundraiserJpaRepository.findById(1L)).thenReturn(Optional.of(fundraiser));

        fundraiserService.closeFundraiser(1L);

        assertEquals(FundraiserStatus.CLOSED, fundraiser.getStatus());
        assertNotNull(fundraiser.getClosedAt());
        verify(fundraiserJpaRepository).save(fundraiser);
    }

    @Test
    void sendMailToDonors_shouldPublishEvent_whenCalled() {
        SendMailingRequest request = new SendMailingRequest(1L, "Subject", "Body");
        
        fundraiserService.sendMailToDonors(request);

        verify(eventPublisher).publishEvent(any(MassMailingRequestedEvent.class));
    }
}