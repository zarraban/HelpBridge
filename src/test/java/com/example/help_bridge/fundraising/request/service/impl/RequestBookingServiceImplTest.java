package com.example.help_bridge.fundraising.request.service.impl;
import com.example.help_bridge.fundraising.request.service.RequestBookingServiceImpl;
import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.event.RequestBookedEvent;
import com.example.help_bridge.fundraising.request.exception.FundNotApprovedException;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    private RequestBookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new RequestBookingServiceImpl(requestRepository, fundRepository, eventPublisher);
    }

    private Request newRequest() {
        return new Request(null, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", "inst", "A-1", true);
    }

    private Request approvedRequest() {
        Request request = newRequest();
        request.transitionTo(RequestStatus.NEW);
        return request;
    }

    private Fund fundWithStatus(FundStatus status) {
        Fund fund = new Fund();
        fund.setStatus(status);
        return fund;
    }

    @Test
    void bookRequest_whenNewAndFundApproved_transitionsToInProgressAndPublishesEvent() {
        Request request = approvedRequest();
        Fund fund = fundWithStatus(FundStatus.APPROVED);
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));
        when(fundRepository.findById(2L)).thenReturn(Optional.of(fund));

        RequestResponse response = bookingService.bookRequest(1L, 2L);

        assertThat(response.status()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(request.getFund()).isSameAs(fund);
        verify(eventPublisher).publishEvent(new RequestBookedEvent(1L, 2L));
    }

    @ParameterizedTest
    @EnumSource(value = FundStatus.class, names = {"PENDING_APPROVAL", "REJECTED"})
    void bookRequest_whenFundNotApproved_throwsAndKeepsRequestUntouched(FundStatus status) {
        Request request = approvedRequest();
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));
        when(fundRepository.findById(2L)).thenReturn(Optional.of(fundWithStatus(status)));

        assertThatThrownBy(() -> bookingService.bookRequest(1L, 2L))
                .isInstanceOf(FundNotApprovedException.class);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.NEW);
        assertThat(request.getFund()).isNull();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void bookRequest_whenAlreadyInProgress_throwsInvalidState() {
        Request request = approvedRequest();
        request.transitionTo(RequestStatus.IN_PROGRESS);
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));
        when(fundRepository.findById(2L)).thenReturn(Optional.of(fundWithStatus(FundStatus.APPROVED)));

        assertThatThrownBy(() -> bookingService.bookRequest(1L, 2L))
                .isInstanceOf(InvalidRequestStateException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void bookRequest_whenRequestMissing_throwsNotFound() {
        when(requestRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.bookRequest(99L, 2L))
                .isInstanceOf(RequestNotFoundException.class);

        verify(fundRepository, never()).findById(any());
    }

    @Test
    void bookRequest_whenFundMissing_throwsFundNotFound() {
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(approvedRequest()));
        when(fundRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.bookRequest(1L, 2L))
                .isInstanceOf(FundNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }
}