package com.example.help_bridge.common.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import com.example.help_bridge.users.auth.AccountType;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Authentication authentication, HttpServletRequest request, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        model.addAttribute("loginEmail", "");
        model.addAttribute("loginAccountType", "APPLICANT");
        var session = request.getSession(false);
        if (session != null) {
            Object previous = session.getAttribute(WebLoginFailureHandler.LAST_USERNAME);
            session.removeAttribute(WebLoginFailureHandler.LAST_USERNAME);
            if (previous instanceof String username && username.contains(":")) {
                String[] parts = username.split(":", 2);
                try {
                    model.addAttribute("loginAccountType", AccountType.fromString(parts[0]).name());
                    model.addAttribute("loginEmail", parts[1].trim());
                } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ignored) {
                    // An unsupported account type is not offered in the form.
                }
            }
        }
        return "login";
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication authentication) {
        if (hasRole(authentication, "APPLICANT")) {
            return "redirect:/requests";
        }
        return "home";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
