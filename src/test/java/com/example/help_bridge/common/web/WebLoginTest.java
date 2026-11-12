package com.example.help_bridge.common.web;

import com.example.help_bridge.fundraising.request.web.RequestWebController;
import com.example.help_bridge.fundraising.request.service.RequestService;
import com.example.help_bridge.fundraising.request.service.RequestVerificationService;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.common.config.SecurityConfiguration;
import com.example.help_bridge.common.security.service.JwtService;
import com.example.help_bridge.common.security.service.JwtValidatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({LoginController.class, RequestWebController.class})
@Import(SecurityConfiguration.class)
class WebLoginTest {
    @Autowired MockMvc mvc;
    @MockitoBean RequestService requestService;
    @MockitoBean RequestVerificationService verificationService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean JwtService jwtService;
    @MockitoBean JwtValidatorService jwtValidatorService;
    @MockitoBean AuthenticationEntryPoint entryPoint;
    @MockitoBean AccessDeniedHandler deniedHandler;

    @Test
    void sharedLayoutRendersRequestPagesAndMessages() throws Exception {
        mvc.perform(get("/requests").with(user("representative").roles("FUND_REPRESENTATIVE"))
                        .flashAttr("message", "Заявку збережено"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Заявку збережено")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("page-header")));
        var applicant = new com.example.help_bridge.fundraising.user.entity.User();
        org.springframework.test.util.ReflectionTestUtils.setField(applicant, "id", 1L);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(java.util.Optional.of(applicant));
        mvc.perform(get("/requests").with(user("APPLICANT:applicant@test.com").roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Мої заявки на допомогу")))
                .andDo(result -> {
                    String html = result.getResponse().getContentAsString();
                    String header = html.substring(html.indexOf("<header"), html.indexOf("</header>"));
                    assertThat(header).contains("href=\"/requests/new\"");
                    assertThat(html.split("Подати нову заявку", -1)).hasSize(2);
                });
        mvc.perform(get("/requests/new").with(user("applicant").roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Опишіть вашу ситуацію")));
    }

    @Test
    void redirectsEveryRoleToItsAvailablePage() throws Exception {
        mvc.perform(get("/home").with(user("applicant").roles("APPLICANT")))
                .andExpect(redirectedUrl("/requests"));
        mvc.perform(get("/home").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(redirectedUrl("/requests"));
        mvc.perform(get("/home").with(user("admin").roles("SYSTEM_ADMIN")))
                .andExpect(redirectedUrl("/requests#verification"));
        mvc.perform(get("/home").with(user("volunteer").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(view().name("home"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ви увійшли як волонтер")));
    }

    @Test
    void loginFormRendersAndAuthenticatedUsersGoHome() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"VOLUNTEER\"")));
        mvc.perform(get("/login").with(user("volunteer").roles("VOLUNTEER")))
                .andExpect(redirectedUrl("/home"));
        mvc.perform(get("/home")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void successfulLoginUsesHomeAndLogoutInvalidatesSession() throws Exception {
        var principal = User.withUsername("VOLUNTEER:vera@test.com").password("hash").roles("VOLUNTEER").build();
        when(authenticationManager.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        var result = mvc.perform(post("/login").with(csrf())
                        .param("username", principal.getUsername()).param("password", "secret"))
                .andExpect(redirectedUrl("/home")).andReturn();
        var session = (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge("JSESSIONID", 0));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/home")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void explainsDisabledAccountsAndInvalidSessions() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));
        mvc.perform(post("/login").with(csrf()).param("username", "VOLUNTEER:vera@test.com").param("password", "secret"))
                .andExpect(redirectedUrl("/login?disabled"));
        mvc.perform(get("/home").with(request -> {
            request.setRequestedSessionId("expired-session");
            request.setRequestedSessionIdValid(false);
            return request;
        })).andExpect(redirectedUrl("/login?expired"));
    }

    @Test
    void rejectsWrongCredentialsAndLoginWithoutCsrf() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException("bad credentials"));
        mvc.perform(post("/login").with(csrf()).param("username", "APPLICANT:wrong@test.com").param("password", "wrong"))
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(post("/login").param("username", "someone").param("password", "secret"))
                .andExpect(redirectedUrl("/login?expired"));
    }
}
