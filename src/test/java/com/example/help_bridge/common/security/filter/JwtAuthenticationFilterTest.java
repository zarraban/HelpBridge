package com.example.help_bridge.common.security.filter;

import com.example.help_bridge.common.security.service.JwtService;
import com.example.help_bridge.common.security.service.JwtValidatorService;
import com.example.help_bridge.users.auth.AppUserDetailsService;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";
    private static final String EMAIL = "vera@vol.test";
    private static final String USERNAME = "VOLUNTEER:" + EMAIL;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private FundRepresentativeRepository fundRepresentativeRepository;

    @Mock
    private SystemAdminRepository systemAdminRepository;

    @Mock
    private AuthenticationEntryPoint authenticationEntryPoint;

    private AppUserDetailsService userDetailsService;
    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userDetailsService = new AppUserDetailsService(fundRepresentativeRepository, systemAdminRepository, volunteerRepository);
        jwtService = new JwtService(SECRET, 15);
        filter = new JwtAuthenticationFilter(new JwtValidatorService(SECRET), userDetailsService, authenticationEntryPoint);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Volunteer volunteer(VolunteerStatus status) {
        Volunteer volunteer = new Volunteer();
        volunteer.setEmail(EMAIL);
        volunteer.setPasswordHash("hash");
        volunteer.setStatus(status);
        return volunteer;
    }

    private String tokenFor(Volunteer volunteer) {
        when(volunteerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(volunteer));
        return jwtService.generateToken(userDetailsService.loadUserByUsername(USERNAME));
    }

    private MockHttpServletRequest requestWith(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/funds");
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        return request;
    }

    @Test
    void authenticatesActiveVolunteer() throws Exception {
        String token = tokenFor(volunteer(VolunteerStatus.ACTIVE));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(USERNAME);
        assertThat(authentication.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_VOLUNTEER");
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(authenticationEntryPoint);
    }

    @Test
    void rejectsTokenOfVolunteerWhoWasDeactivatedAfterLogin() throws Exception {
        Volunteer volunteer = volunteer(VolunteerStatus.ACTIVE);
        String token = tokenFor(volunteer);
        volunteer.setStatus(VolunteerStatus.INACTIVE);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), chain);

        ArgumentCaptor<AuthenticationException> captor = ArgumentCaptor.forClass(AuthenticationException.class);
        verify(authenticationEntryPoint).commence(any(), any(), captor.capture());
        assertThat(captor.getValue().getCause()).isInstanceOf(DisabledException.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void rejectsTokenOfVolunteerWhoWasDeletedAfterLogin() throws Exception {
        String token = tokenFor(volunteer(VolunteerStatus.ACTIVE));
        when(volunteerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), chain);

        verify(authenticationEntryPoint).commence(any(), any(), any(AuthenticationException.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void rejectsTamperedToken() throws Exception {
        String token = tokenFor(volunteer(VolunteerStatus.ACTIVE));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWith(token + "x"), new MockHttpServletResponse(), chain);

        verify(authenticationEntryPoint).commence(any(), any(), any(AuthenticationException.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void passesRequestWithoutTokenOnUnauthenticated() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWith(null), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(authenticationEntryPoint, volunteerRepository);
    }
}
