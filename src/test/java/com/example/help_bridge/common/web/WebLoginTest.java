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

@WebMvcTest({LoginController.class, RequestWebController.class, com.example.help_bridge.fundraising.request.web.RequestDocumentWebController.class})
@Import(SecurityConfiguration.class)
class WebLoginTest {
    @MockitoBean com.example.help_bridge.fundraising.request.security.RequestAccessPolicy access;
    @MockitoBean com.example.help_bridge.fundraising.request.service.RequestDocumentService documentService;
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
    void concurrentEditKeepsSubmittedDataAndBlocksRepeatSave() throws Exception {
        var request = new com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse(
                1L, 1L, null, "MEDICAL", java.math.BigDecimal.TEN, java.time.LocalDate.now().plusDays(10),
                "Situation", "Need", "Clinic", "DOC", com.example.help_bridge.fundraising.request.entity.RequestStatus.PENDING_VERIFICATION,
                false, java.time.LocalDateTime.now(), java.util.List.of());
        when(requestService.getRequestById(1L)).thenReturn(request);
        when(access.currentViewer()).thenReturn(new com.example.help_bridge.fundraising.request.security.RequestAccessPolicy.Viewer("APPLICANT", 1L, null));
        when(requestService.updateRequest(org.mockito.ArgumentMatchers.eq(1L), any()))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("Request", 1L));
        mvc.perform(post("/requests/1").with(user("applicant").roles("APPLICANT")).with(csrf())
                        .param("assistanceType", "MEDICAL").param("amount", "100")
                        .param("deadline", java.time.LocalDate.now().plusDays(10).toString())
                        .param("situationDescription", "Submitted situation").param("needDescription", "Submitted need")
                        .param("institutionName", "Submitted clinic").param("applicationNumber", "DOC"))
                .andExpect(status().isConflict()).andExpect(view().name("requests/edit-form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"Submitted clinic\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(">Зберегти зміни</button>"))));
    }

    @Test
    void webErrorsHaveCorrectStatusesAndSafeRecoveryActions() throws Exception {
        when(requestService.getSharedPool()).thenThrow(new IllegalStateException("private database details"));
        mvc.perform(get("/requests").with(user("representative").roles("FUND_REPRESENTATIVE"))
                        .accept(org.springframework.http.MediaType.TEXT_HTML))
                .andExpect(status().isInternalServerError()).andExpect(view().name("error/500"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("спочатку перевірте результат")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private database details"))));
        org.mockito.Mockito.doThrow(new com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException("changed"))
                .when(requestService).getSharedPool();
        mvc.perform(get("/requests").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isConflict()).andExpect(view().name("error/409"));
        org.mockito.Mockito.doThrow(new com.example.help_bridge.fundraising.request.exception.RequestNotFoundException(1L))
                .when(requestService).getSharedPool();
        mvc.perform(get("/requests").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isNotFound()).andExpect(view().name("error/404"));
        org.mockito.Mockito.doThrow(new org.springframework.security.access.AccessDeniedException("denied"))
                .when(requestService).getSharedPool();
        mvc.perform(get("/requests").with(user("representative").roles("FUND_REPRESENTATIVE"))
                        .accept(org.springframework.http.MediaType.TEXT_HTML))
                .andExpect(status().isForbidden()).andExpect(view().name("error/403"));
    }

    @Test
    void emptyPoolAndReviewConflictOfferNextActions() throws Exception {
        mvc.perform(get("/requests").with(user("admin").roles("SYSTEM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Спільний пул поки порожній")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Оновити чергу")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Оновити список")));
        org.mockito.Mockito.doThrow(new com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException("changed"))
                .when(verificationService).reviewRequest(org.mockito.ArgumentMatchers.eq(1L), any());
        mvc.perform(post("/requests/1/review").with(user("admin").roles("SYSTEM_ADMIN"))
                        .with(csrf()).param("approved", "true"))
                .andExpect(redirectedUrl("/requests#verification"))
                .andExpect(flash().attribute("error", org.hamcrest.Matchers.containsString("Список оновлено")));
        mvc.perform(post("/requests/1/review").with(user("admin").roles("SYSTEM_ADMIN"))
                        .with(csrf()).param("approved", "invalid"))
                .andExpect(status().isBadRequest()).andExpect(view().name("error/4xx"));
    }

    @Test
    void failedLoginRetainsEmailAndRoleWithoutPassword() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException("bad credentials"));
        var failed = mvc.perform(post("/login").with(csrf())
                        .param("username", "VOLUNTEER:vera@test.com").param("password", "private-password"))
                .andExpect(redirectedUrl("/login?error")).andReturn();
        var session = (org.springframework.mock.web.MockHttpSession) failed.getRequest().getSession(false);
        mvc.perform(get("/login?error").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"vera@test.com\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"VOLUNTEER\" selected=\"selected\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-password"))));
    }

    @Test
    void invalidRequestKeepsValuesAndShowsOtherTypeField() throws Exception {
        when(requestService.getAssistanceTypes()).thenReturn(java.util.Map.of("OTHER", "Інше"));
        mvc.perform(post("/requests").with(user("applicant").roles("APPLICANT")).with(csrf())
                        .param("assistanceType", "OTHER").param("otherAssistanceType", "Обладнання")
                        .param("needDescription", "Потрібен генератор").param("amount", "-5"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"Обладнання\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Потрібен генератор")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"-5\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("введені дані збережено")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("style=\"display:none\""))));
    }

    @Test
    void hotRequestKeepsItsStatusAndInvalidFieldsAreMarked() throws Exception {
        var request = new com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse(
                1L, 1L, null, "MEDICAL", java.math.BigDecimal.TEN,
                java.time.LocalDate.now().minusDays(1), "Ситуація", "Допомога", "Установа", "DOC-1",
                com.example.help_bridge.fundraising.request.entity.RequestStatus.NEW, true,
                java.time.LocalDateTime.now(), java.util.List.of());
        when(requestService.getSharedPool()).thenReturn(java.util.List.of(request));
        when(requestService.getAssistanceTypes()).thenReturn(java.util.Map.of("MEDICAL", "Лікування"));
        mvc.perform(get("/requests").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"badge\">Нова")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Гаряча")));
        mvc.perform(post("/requests").with(user("applicant").roles("APPLICANT")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("aria-invalid=\"true\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("field-invalid")));
    }

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
