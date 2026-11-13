package com.example.help_bridge.fundraising.request.security;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy.Viewer;
import com.example.help_bridge.fundraising.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.access.AccessDeniedException;

import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestAccessPolicyTest {
    private Request request(RequestStatus status, Long fundId) {
        Request request = mock(Request.class);
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);
        when(request.getRequester()).thenReturn(owner);
        when(request.getStatus()).thenReturn(status);
        if (fundId != null) {
            Fund fund = mock(Fund.class);
            when(fund.getId()).thenReturn(fundId);
            when(request.getFund()).thenReturn(fund);
        }
        return request;
    }

    @ParameterizedTest
    @EnumSource(RequestStatus.class)
    void roleAndStatusMatrix(RequestStatus status) {
        var request = request(status, null);
        boolean pending = status == RequestStatus.PENDING_VERIFICATION;
        var admin = new Viewer("SYSTEM_ADMIN", null, null).permissions(request);
        assertThat(admin.view()).isTrue();
        assertThat(admin.review()).isEqualTo(pending);
        assertThat(admin.book()).isFalse();
        assertThat(admin.edit()).isEqualTo(pending);

        var owner = new Viewer("APPLICANT", 1L, null).permissions(request);
        assertThat(owner.view()).isTrue();
        assertThat(owner.edit()).isEqualTo(pending);
        assertThat(owner.manageDocuments()).isEqualTo(pending);
        assertThat(owner.delete()).isEqualTo(pending || status == RequestStatus.NEW);
        assertThat(owner.review()).isFalse();
        assertThat(owner.book()).isFalse();

        var stranger = new Viewer("APPLICANT", 2L, null).permissions(request);
        assertThat(stranger.view()).isFalse();
        assertThat(stranger.edit()).isFalse();
        assertThat(stranger.delete()).isFalse();
        assertThat(stranger.manageDocuments()).isFalse();

        var rep = new Viewer("FUND_REPRESENTATIVE", null, 10L, FundStatus.APPROVED).permissions(request);
        assertThat(rep.view()).isEqualTo(status == RequestStatus.NEW);
        assertThat(rep.book()).isEqualTo(status == RequestStatus.NEW);
        assertThat(rep.review()).isFalse();
        assertThat(rep.edit()).isFalse();
        assertThat(rep.delete()).isFalse();
        assertThat(rep.manageDocuments()).isFalse();
        assertThat(rep.ownFundInProgress()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(FundStatus.class)
    void onlyApprovedFundCanBook(FundStatus status) {
        var viewer = new Viewer("FUND_REPRESENTATIVE", null, 10L, status);
        var request = request(RequestStatus.NEW, null);
        assertThat(viewer.permissions(request).book()).isEqualTo(status == FundStatus.APPROVED);
        if (status == FundStatus.APPROVED) assertThatCode(() -> viewer.requireBook(request)).doesNotThrowAnyException();
        else assertThatThrownBy(() -> viewer.requireBook(request)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void bookingDefaultsToDeniedWithoutVerifiedFundContext() {
        var request = request(RequestStatus.NEW, null);
        for (var viewer : new Viewer[]{new Viewer("FUND_REPRESENTATIVE", null, 10L),
                new Viewer("FUND_REPRESENTATIVE", null, null, FundStatus.APPROVED),
                new Viewer("SYSTEM_ADMIN", null, null), new Viewer("APPLICANT", 1L, null)}) {
            assertThat(viewer.permissions(request).book()).isFalse();
            assertThatThrownBy(() -> viewer.requireBook(request)).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void ownFundProgressAndForeignFundAccess() {
        var viewer = new Viewer("FUND_REPRESENTATIVE", null, 10L, FundStatus.APPROVED);
        var own = request(RequestStatus.IN_PROGRESS, 10L);
        var foreign = request(RequestStatus.IN_PROGRESS, 20L);
        assertThat(viewer.permissions(own).view()).isTrue();
        assertThat(viewer.permissions(own).ownFundInProgress()).isTrue();
        assertThat(viewer.permissions(own).book()).isFalse();
        assertThat(viewer.permissions(foreign).view()).isFalse();
        assertThat(viewer.permissions(foreign).ownFundInProgress()).isFalse();
        assertThatThrownBy(() -> viewer.requireBook(own)).isInstanceOf(InvalidRequestStateException.class);
        assertThatThrownBy(() -> viewer.requireRead(foreign)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void currentViewerLoadsActualFundStatus() {
        var users = mock(UserRepository.class);
        var representatives = mock(FundRepresentativeRepository.class);
        var rep = mock(FundRepresentative.class);
        var fund = mock(Fund.class);
        when(rep.getFund()).thenReturn(fund);
        when(fund.getId()).thenReturn(10L);
        when(representatives.findByEmailIgnoreCase("rep@test.com")).thenReturn(Optional.of(rep));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                "FUND_REPRESENTATIVE:rep@test.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_FUND_REPRESENTATIVE"))));
        SecurityContextHolder.setContext(context);
        try {
            var policy = new RequestAccessPolicy(users, representatives);
            var request = request(RequestStatus.NEW, null);
            when(fund.getStatus()).thenReturn(FundStatus.APPROVED);
            assertThat(policy.currentViewer().permissions(request).book()).isTrue();
            when(fund.getStatus()).thenReturn(FundStatus.REJECTED);
            assertThat(policy.currentViewer().permissions(request).book()).isFalse();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void reviewRequiresAdminAndPendingState() {
        var admin = new Viewer("SYSTEM_ADMIN", null, null);
        var pending = request(RequestStatus.PENDING_VERIFICATION, null);
        assertThatCode(() -> admin.requireReview(pending)).doesNotThrowAnyException();
        assertThatThrownBy(() -> admin.requireReview(request(RequestStatus.NEW, null)))
                .isInstanceOf(InvalidRequestStateException.class);
        assertThatThrownBy(() -> new Viewer("APPLICANT", 1L, null).requireReview(pending))
                .isInstanceOf(AccessDeniedException.class);
    }
}
