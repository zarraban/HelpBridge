package com.example.help_bridge.applicant.service.impl;

import com.example.help_bridge.request.entity.Request;
import com.example.help_bridge.request.entity.RequestStatus;
import com.example.help_bridge.request.event.RequestBookedEvent;
import com.example.help_bridge.request.exception.InvalidRequestStateException;
import com.example.help_bridge.request.exception.RequestNotFoundException;
import com.example.help_bridge.request.repository.RequestRepository;
import com.example.help_bridge.request.service.RequestBookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestBookingServiceImplTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RequestBookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new RequestBookingServiceImpl(requestRepository, eventPublisher);
    }

    @Test
    void bookRequest_whenNew_transitionsToInProgressAndPublishesEvent() {
        Request request = new Request(1L, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", "inst", "A-1");
        request.transitionTo(RequestStatus.NEW);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookingService.bookRequest(1L, 2L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        verify(eventPublisher).publishEvent(new RequestBookedEvent(1L, 2L));
    }

    @Test
    void bookRequest_whenAlreadyInProgress_throwsInvalidState() {
        Request request = new Request(1L, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", "inst", "A-1");
        request.transitionTo(RequestStatus.NEW);
        request.transitionTo(RequestStatus.IN_PROGRESS);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> bookingService.bookRequest(1L, 2L))
                .isInstanceOf(InvalidRequestStateException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void bookRequest_whenRequestMissing_throwsNotFound() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.bookRequest(99L, 2L))
                .isInstanceOf(RequestNotFoundException.class);
    }
}