package com.example.help_bridge.fundraising.fundraiser.listener;

import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraising.request.event.RequestBookedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestBookedListenerTest {

    @Mock
    private FundraiserRepository fundraiserRepository;

    @InjectMocks
    private RequestBookedListener listener;

    @Test
    void onRequestBooked_createsPendingFundraiser() {
        when(fundraiserRepository.existsByRequestId(10L)).thenReturn(false);

        listener.onRequestBooked(new RequestBookedEvent(10L, 2L));

        ArgumentCaptor<Fundraiser> captor = ArgumentCaptor.forClass(Fundraiser.class);
        verify(fundraiserRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getRequestId());
        assertEquals(FundraiserStatus.PENDING_ASSIGNMENT, captor.getValue().getStatus());
    }

    @Test
    void onRequestBooked_isIdempotent() {
        when(fundraiserRepository.existsByRequestId(10L)).thenReturn(true);

        listener.onRequestBooked(new RequestBookedEvent(10L, 2L));

        verify(fundraiserRepository, never()).save(any());
    }
}
