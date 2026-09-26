package com.example.help_bridge.applicant.service.impl;

import com.example.help_bridge.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.request.entity.Request;
import com.example.help_bridge.request.entity.RequestStatus;
import com.example.help_bridge.request.exception.RequestNotFoundException;
import com.example.help_bridge.request.repository.RequestRepository;
import com.example.help_bridge.request.service.RequestVerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestVerificationServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    private RequestVerificationServiceImpl verificationService;
    private Request pendingRequest;

    @BeforeEach
    void setUp() {
        verificationService = new RequestVerificationServiceImpl(requestRepository);
        pendingRequest = new Request(1L, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", "inst", "A-1");
    }

    @Test
    void reviewRequest_whenApproved_transitionsToNewAndSaves() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));

        verificationService.reviewRequest(1L, new RequestVerificationDto(true, "ok"));

        assertThat(pendingRequest.getStatus()).isEqualTo(RequestStatus.NEW);
        verify(requestRepository).save(pendingRequest);
    }

    @Test
    void reviewRequest_whenRejected_deletesRequest() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));

        verificationService.reviewRequest(1L, new RequestVerificationDto(false, "fraud"));

        verify(requestRepository).deleteById(1L);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void reviewRequest_whenRequestMissing_throwsNotFound() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.reviewRequest(99L, new RequestVerificationDto(true, null)))
                .isInstanceOf(RequestNotFoundException.class);
    }
}