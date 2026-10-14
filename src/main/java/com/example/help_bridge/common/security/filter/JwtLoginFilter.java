package com.example.help_bridge.common.security.filter;

import com.example.help_bridge.common.security.dto.request.LoginRequest;
import com.example.help_bridge.common.security.dto.response.AuthResponse;
import com.example.help_bridge.common.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JwtLoginFilter extends AbstractAuthenticationProcessingFilter {

    public static final String LOGIN_PATH = "/api/auth/login";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtLoginFilter(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          ObjectMapper objectMapper,
                          AuthenticationEntryPoint authenticationEntryPoint) {
        super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, LOGIN_PATH), authenticationManager);
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        LoginRequest login = readLoginRequest(request);
        String username = login.accountType() + ":" + login.email().trim();
        return getAuthenticationManager().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, login.password()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        String token = jwtService.generateToken((UserDetails) authResult.getPrincipal());
        AuthResponse body = AuthResponse.of(token, jwtService.getExpirationMillis() / 1000);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        authenticationEntryPoint.commence(request, response, failed);
    }

    private LoginRequest readLoginRequest(HttpServletRequest request) {
        LoginRequest login;
        try {
            login = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (RuntimeException | IOException e) {
            throw new BadCredentialsException("Malformed login request", e);
        }
        if (login == null || login.accountType() == null
                || isBlank(login.email()) || isBlank(login.password())) {
            throw new BadCredentialsException("Malformed login request");
        }
        return login;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
