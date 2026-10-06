package com.example.help_bridge.fundraising.request.service.impl;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.UpdateRequestRequest;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequesterNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.service.RequestServiceImpl;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;

    private RequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        requestService = new RequestServiceImpl(requestRepository, userRepository);
    }

    private CreateRequestRequest dto(Long userId) {
        return new CreateRequestRequest(userId, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(10), "s", "n", "inst", "A-1", true);
    }

    private UpdateRequestRequest updateDto() {
        return new UpdateRequestRequest("SURGERY", new BigDecimal("2000.00"),
                LocalDate.now().plusDays(20), "s2", "n2", "inst2", "A-2");
    }

    private Request pendingRequest(LocalDate deadline) {
        return new Request(new User(), "MEDICAL", BigDecimal.TEN,
                deadline, "s", "n", "inst", "A-1", true);
    }

    private Request newRequest(LocalDate deadline) {
        Request request = pendingRequest(deadline);
        request.approve();
        return request;
    }

    @Test
    void createRequest_withExistingUser_savesWithPendingVerificationStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        RequestResponse response = requestService.createRequest(dto(1L));

        assertThat(response.status()).isEqualTo(RequestStatus.PENDING_VERIFICATION);
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void createRequest_whenUserMissing_throwsRequesterNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.createRequest(dto(5L)))
                .isInstanceOf(RequesterNotFoundException.class);

        verify(requestRepository, never()).save(any());
    }

    @Test
    void getSharedPool_returnsExpiredFirstThenByDeadline() {
        LocalDate expired = LocalDate.now().minusDays(1);
        LocalDate soon = LocalDate.now().plusDays(2);
        LocalDate later = LocalDate.now().plusDays(10);

        when(requestRepository.findAllByStatusWithDetails(RequestStatus.NEW))
                .thenReturn(List.of(newRequest(later), newRequest(expired), newRequest(soon)));

        List<RequestResponse> result = requestService.getSharedPool();

        assertThat(result).extracting(RequestResponse::deadline)
                .containsExactly(expired, soon, later);
    }

    @Test
    void getRequestById_whenMissing_throwsNotFound() {
        when(requestRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(99L))
                .isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    void updateRequest_whenPendingVerification_updatesDetails() {
        Request request = pendingRequest(LocalDate.now().plusDays(5));
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        RequestResponse response = requestService.updateRequest(1L, updateDto());

        assertThat(response.assistanceType()).isEqualTo("SURGERY");
        assertThat(response.institutionName()).isEqualTo("inst2");
        assertThat(response.applicationNumber()).isEqualTo("A-2");
    }

    @Test
    void updateRequest_whenAlreadyApproved_throwsInvalidStateAndDoesNotSave() {
        Request request = newRequest(LocalDate.now().plusDays(5));
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.updateRequest(1L, updateDto()))
                .isInstanceOf(InvalidRequestStateException.class);

        verify(requestRepository, never()).save(any());
    }

    @Test
    void deleteRequest_whenMissing_throwsNotFound() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.deleteRequest(99L))
                .isInstanceOf(RequestNotFoundException.class);

        verify(requestRepository, never()).delete(any());
    }

    @Test
    void deleteRequest_whenInProgress_throwsInvalidStateAndDoesNotDelete() {
        Request request = newRequest(LocalDate.now().plusDays(5));
        request.transitionTo(RequestStatus.IN_PROGRESS);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.deleteRequest(1L))
                .isInstanceOf(InvalidRequestStateException.class);

        verify(requestRepository, never()).delete(any());
    }

    @Test
    void deleteRequest_whenPendingVerification_deletes() {
        Request request = pendingRequest(LocalDate.now().plusDays(5));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        requestService.deleteRequest(1L);

        verify(requestRepository).delete(request);
    }
}