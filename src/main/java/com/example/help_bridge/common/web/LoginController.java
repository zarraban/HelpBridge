package com.example.help_bridge.common.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication authentication) {
        if (hasRole(authentication, "SYSTEM_ADMIN")) {
            return "redirect:/requests#verification";
        }
        if (hasRole(authentication, "APPLICANT") || hasRole(authentication, "FUND_REPRESENTATIVE")) {
            return "redirect:/requests";
        }
        return "home";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
