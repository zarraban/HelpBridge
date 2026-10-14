package com.example.help_bridge.common.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelpBridgeAuthenticationManagerTest {

    private static final String USERNAME = "VOLUNTEER:anna@gmail.com";
    private static final String PASSWORD = "Secret123!";

    @Mock
    private UserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private HelpBridgeAuthenticationManager manager;

    @BeforeEach
    void setUp() {
        manager = new HelpBridgeAuthenticationManager(userDetailsService, passwordEncoder);
    }

    private UserDetails user(boolean enabled) {
        return User.withUsername(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .roles("VOLUNTEER")
                .disabled(!enabled)
                .build();
    }

    private Authentication request(String password) {
        return new UsernamePasswordAuthenticationToken(USERNAME, password);
    }

    @Test
    void authenticatesWhenPasswordMatches() {
        UserDetails user = user(true);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(user);

        Authentication result = manager.authenticate(request(PASSWORD));

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isSameAs(user);
        assertThat(result.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_VOLUNTEER");
    }

    @Test
    void throwsBadCredentialsWhenPasswordIsWrong() {
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(user(true));

        assertThatThrownBy(() -> manager.authenticate(request("wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void throwsBadCredentialsWhenAccountDoesNotExist() {
        when(userDetailsService.loadUserByUsername(USERNAME)).thenThrow(new UsernameNotFoundException("x"));

        assertThatThrownBy(() -> manager.authenticate(request(PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void throwsDisabledWhenPasswordMatchesButAccountIsDisabled() {
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(user(false));

        assertThatThrownBy(() -> manager.authenticate(request(PASSWORD)))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void doesNotRevealDisabledAccountWhenPasswordIsWrong() {
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(user(false));

        assertThatThrownBy(() -> manager.authenticate(request("wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void throwsBadCredentialsWhenCredentialsAreMissing() {
        assertThatThrownBy(() -> manager.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, null)))
                .isInstanceOf(BadCredentialsException.class);
    }
}
