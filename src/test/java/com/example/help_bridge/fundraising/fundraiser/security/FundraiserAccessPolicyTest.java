package com.example.help_bridge.fundraising.fundraiser.security;

import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundraiserAccessPolicyTest {
    @Mock RequestAccessPolicy requestAccess;
    @Mock RequestRepository requests;
    @InjectMocks FundraiserAccessPolicy access;

    private Fundraiser fundraiser() {
        var fundraiser = new Fundraiser();
        fundraiser.setRequestId(10L);
        return fundraiser;
    }

    @Test
    void adminCanAccessEvenWithoutLinkedRequest() {
        when(requestAccess.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("SYSTEM_ADMIN", null, null));
        assertDoesNotThrow(() -> access.requireAccess(new Fundraiser()));
        verifyNoInteractions(requests);
    }

    @Test
    void representativeCanAccessOwnFundraiser() {
        when(requestAccess.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("FUND_REPRESENTATIVE", null, 2L));
        when(requests.existsByIdAndFundId(10L, 2L)).thenReturn(true);
        assertDoesNotThrow(() -> access.requireAccess(fundraiser()));
    }

    @Test
    void representativeCannotAccessForeignOrUnbookedRequest() {
        when(requestAccess.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("FUND_REPRESENTATIVE", null, 2L));
        assertThrows(AccessDeniedException.class, () -> access.requireAccess(fundraiser()));
    }

    @Test
    void representativeWithoutFundIsDenied() {
        when(requestAccess.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("FUND_REPRESENTATIVE", null, null));
        assertThrows(AccessDeniedException.class, access::currentViewer);
    }

    @Test
    void applicantCannotReadFundraisersEvenWhenOwningRequest() {
        when(requestAccess.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("APPLICANT", 1L, null));
        assertThrows(AccessDeniedException.class, () -> access.requireAccess(fundraiser()));
        verifyNoInteractions(requests);
    }
}
