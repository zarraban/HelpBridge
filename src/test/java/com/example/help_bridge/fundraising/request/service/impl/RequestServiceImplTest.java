package com.example.help_bridge.fundraising.request.service.impl;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
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

    private Request newRequest(LocalDate deadline) {
        Request request = new Request(null, "MEDICAL", BigDecimal.TEN,
                deadline, "s", "n", "inst", "A-1", true);
        request.transitionTo(RequestStatus.NEW);
        return request;
    }

    @Test
    void createRequest_withoutUser_savesWithPendingVerificationStatus() {
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        RequestResponse response = requestService.createRequest(dto(1L));

        assertThat(response.status()).isEqualTo(RequestStatus.PENDING_VERIFICATION);
        verify(userRepository).findById(any());
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
    void deleteRequest_whenMissing_throwsNotFound() {
        when(requestRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> requestService.deleteRequest(99L))
                .isInstanceOf(RequestNotFoundException.class);

        verify(requestRepository, never()).deleteById(any());
    }
}