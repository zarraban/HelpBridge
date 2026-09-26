package com.example.help_bridge.fund.service;

import com.example.help_bridge.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fund.dto.request.FundDescriptUpdateRequest;
import com.example.help_bridge.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fund.dto.response.FundResponse;
import com.example.help_bridge.fund.entity.Fund;
import com.example.help_bridge.fund.entity.FundStatus;
import com.example.help_bridge.fund.event.FundStatusChangedEvent;
import com.example.help_bridge.fund.exception.DuplicateFundException;
import com.example.help_bridge.fund.exception.FundNotFoundException;
import com.example.help_bridge.fund.exception.InvalidFundStatusTransitionException;
import com.example.help_bridge.fund.repository.FundRepository;
import com.example.help_bridge.fund.service.impl.FundServiceImpl;
import com.example.help_bridge.fund.strategy.FundStatusTransitionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundServiceImplTest {

    @Mock
    private FundRepository fundRepository;

    @Mock
    private FundStatusTransitionHandler approvedHandler;

    @Mock
    private FundStatusTransitionHandler rejectedHandler;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private FundServiceImpl fundService;

    @BeforeEach
    void setUp() {
        fundService = new FundServiceImpl(
                fundRepository,
                List.of(approvedHandler, rejectedHandler),
                eventPublisher
        );
    }

    private Fund pendingFund() {
        Fund fund = new Fund();
        fund.setId(1L);
        fund.setFundName("Help Bridge");
        fund.setStatus(FundStatus.PENDING_APPROVAL);
        return fund;
    }

    @Test
    void getFundById_returnsFund_whenExists() {
        when(fundRepository.findById(1L)).thenReturn(Optional.of(pendingFund()));

        FundResponse response = fundService.getFundById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(FundStatus.PENDING_APPROVAL);
    }

    @Test
    void getFundById_throwsNotFound_whenMissing() {
        when(fundRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fundService.getFundById(99L))
                .isInstanceOf(FundNotFoundException.class);
    }

    @Test
    void createFund_setsStatusToPendingApproval() {
        when(fundRepository.save(any(Fund.class))).thenAnswer(invocation -> {
            Fund fund = invocation.getArgument(0);
            fund.setId(1L);
            return fund;
        });

        FundCreateRequest request = new FundCreateRequest(
                "Daria", "Chorna", "Help Bridge", "12345678",
                "UA123456", "Kyiv, 1", "Kyiv, 2", "+38000000000",
                "info@helpbridge.ua", "https://helpbridge.ua", null
        );

        FundResponse response = fundService.createFund(request);

        assertThat(response.status()).isEqualTo(FundStatus.PENDING_APPROVAL);
        verify(fundRepository).save(any(Fund.class));
    }

    @Test
    void updateFundStatus_approvesFund_dispatchesHandlerAndPublishesEvent() {
        Fund fund = pendingFund();
        when(fundRepository.findById(1L)).thenReturn(Optional.of(fund));
        when(fundRepository.save(any(Fund.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(approvedHandler.supports(FundStatus.APPROVED)).thenReturn(true);
        when(rejectedHandler.supports(FundStatus.APPROVED)).thenReturn(false);

        FundResponse response = fundService.updateFundStatus(1L, new FundStatusUpdateRequest(FundStatus.APPROVED));

        assertThat(response.status()).isEqualTo(FundStatus.APPROVED);
        verify(approvedHandler).handle(fund);
        verify(rejectedHandler, never()).handle(any());

        ArgumentCaptor<FundStatusChangedEvent> captor = ArgumentCaptor.forClass(FundStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().previousStatus()).isEqualTo(FundStatus.PENDING_APPROVAL);
        assertThat(captor.getValue().newStatus()).isEqualTo(FundStatus.APPROVED);
    }

    @Test
    void updateFundStatus_throwsInvalidTransition_whenAlreadyDecided() {
        Fund fund = pendingFund();
        fund.setStatus(FundStatus.APPROVED);
        when(fundRepository.findById(1L)).thenReturn(Optional.of(fund));

        assertThatThrownBy(() -> fundService.updateFundStatus(1L, new FundStatusUpdateRequest(FundStatus.REJECTED)))
                .isInstanceOf(InvalidFundStatusTransitionException.class);

        verify(fundRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateFundStatus_throwsNotFound_whenFundMissing() {
        when(fundRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fundService.updateFundStatus(42L, new FundStatusUpdateRequest(FundStatus.APPROVED)))
                .isInstanceOf(FundNotFoundException.class);
    }

    @Test
    void updateFundDescription_updatesDescription() {
        Fund fund = pendingFund();
        when(fundRepository.findById(1L)).thenReturn(Optional.of(fund));
        when(fundRepository.save(any(Fund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FundResponse response = fundService.updateFundDescription(1L, new FundDescriptUpdateRequest("New description"));

        assertThat(response.description()).isEqualTo("New description");
    }

    @Test
    void deleteFundById_deletes_whenExists() {
        when(fundRepository.findById(1L)).thenReturn(Optional.of(pendingFund()));

        fundService.deleteFundById(1L);

        verify(fundRepository).deleteById(1L);
    }

    @Test
    void deleteFundById_throwsNotFound_whenMissing() {
        when(fundRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fundService.deleteFundById(1L))
                .isInstanceOf(FundNotFoundException.class);

        verify(fundRepository, never()).deleteById(any());
    }

    @Test
    void createFund_throwsDuplicate_whenEdrpouExists() {
        when(fundRepository.existsByEdrpou("12345678")).thenReturn(true);

        FundCreateRequest request = new FundCreateRequest(
                "Daria", "Chorna", "Help Bridge", "12345678",
                "UA123456", "Kyiv, 1", "Kyiv, 2", "+0670000000",
                "info@helpbridge.ua", "https://helpbridge.ua", null
        );

        assertThatThrownBy(() -> fundService.createFund(request))
                .isInstanceOf(DuplicateFundException.class);

        verify(fundRepository, never()).save(any());
    }
}
