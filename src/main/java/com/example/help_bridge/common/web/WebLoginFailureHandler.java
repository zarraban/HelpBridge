package com.example.help_bridge.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class WebLoginFailureHandler implements AuthenticationFailureHandler {
    public static final String LAST_USERNAME = "helpbridge.lastLoginUsername";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        if (username != null && username.length() <= 300) {
            request.getSession().setAttribute(LAST_USERNAME, username);
        }
        response.sendRedirect(request.getContextPath()
                + (exception instanceof DisabledException ? "/login?disabled" : "/login?error"));
    }
}
