package com.example.help_bridge.common.security.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HelpBridgeAuthenticationManager implements AuthenticationManager {

    private static final String BAD_CREDENTIALS = "Bad credentials";

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public HelpBridgeAuthenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("help-bridge-dummy-password");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Object credentials = authentication.getCredentials();
        if (authentication.getName() == null || credentials == null) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }
        String rawPassword = credentials.toString();

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(authentication.getName());
        } catch (UsernameNotFoundException e) {
            passwordEncoder.matches(rawPassword, dummyHash);
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        UsernamePasswordAuthenticationToken result =
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }
}
