package com.example.help_bridge.fundraising.request.service.impl;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.event.RequestBookedEvent;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.service.RequestBookingServiceImpl;
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
    private FundRepository fundRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private Fund fund;

    private RequestBookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new RequestBookingServiceImpl(requestRepository, fundRepository, eventPublisher);
    }

    private Request newRequest() {
        return new Request(null, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", "inst", "A-1", true);
    }

    @Test
    void bookRequest_whenNew_transitionsToInProgressAndPublishesEvent() {
        Request request = newRequest();
        request.transitionTo(RequestStatus.NEW);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(fundRepository.findById(2L)).thenReturn(Optional.of(fund));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookingService.bookRequest(1L, 2L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(request.getFund()).isSameAs(fund);
        verify(eventPublisher).publishEvent(new RequestBookedEvent(1L, 2L));
    }

    @Test
    void bookRequest_whenAlreadyInProgress_throwsInvalidState() {
        Request request = newRequest();
        request.transitionTo(RequestStatus.NEW);
        request.transitionTo(RequestStatus.IN_PROGRESS);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(fundRepository.findById(2L)).thenReturn(Optional.of(fund));

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

    @Test
    void bookRequest_whenFundMissing_throwsFundNotFound() {
        Request request = newRequest();
        request.transitionTo(RequestStatus.NEW);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(fundRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.bookRequest(1L, 2L))
                .isInstanceOf(FundNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }
}