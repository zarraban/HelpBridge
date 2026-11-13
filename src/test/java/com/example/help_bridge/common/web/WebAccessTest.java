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
class WebAccessTest {
    @MockitoBean com.example.help_bridge.fundraising.request.security.RequestAccessPolicy access;
    @MockitoBean com.example.help_bridge.fundraising.request.service.RequestDocumentService documentService;
    @Autowired MockMvc mvc;
    @MockitoBean RequestService requestService;
    @MockitoBean RequestVerificationService verificationService;
    @MockitoBean com.example.help_bridge.fundraising.fundraiser.service.FundraiserService fundraisers;
    @MockitoBean UserRepository userRepository;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean JwtService jwtService;
    @MockitoBean JwtValidatorService jwtValidatorService;
    @MockitoBean AuthenticationEntryPoint entryPoint;
    @MockitoBean AccessDeniedHandler deniedHandler;

    @Test
    void guestsMustLogInBeforeOpeningPagesOrPerformingActions() throws Exception {
        for (String path : new String[]{"/", "/home", "/requests", "/requests/new", "/requests/1", "/requests/1/delete", "/requests/1/documents/new"}) {
            mvc.perform(get(path)).andExpect(redirectedUrl("/login"));
        }
        mvc.perform(post("/requests").with(csrf())).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/requests/1/review").with(csrf()).param("approved", "true"))
                .andExpect(redirectedUrl("/login"));
        org.mockito.Mockito.verifyNoInteractions(requestService, verificationService, userRepository);
    }

    @Test
    void directLinksAndPostsRespectEveryRole() throws Exception {
        for (String role : new String[]{"APPLICANT", "FUND_REPRESENTATIVE", "SYSTEM_ADMIN", "VOLUNTEER"}) {
            if (!role.equals("APPLICANT")) {
                mvc.perform(get("/requests/new").with(user("test").roles(role)))
                        .andExpect(status().isForbidden());
                mvc.perform(post("/requests").with(user("test").roles(role)).with(csrf()))
                        .andExpect(status().isForbidden());
            }
            if (!role.equals("APPLICANT") && !role.equals("SYSTEM_ADMIN")) {
                mvc.perform(get("/requests/1/documents/new").with(user("test").roles(role)))
                        .andExpect(status().isForbidden());
                mvc.perform(post("/requests/1/documents").with(user("test").roles(role)).with(csrf()))
                        .andExpect(status().isForbidden());
                mvc.perform(post("/requests/1/documents/1/delete").with(user("test").roles(role)).with(csrf()))
                        .andExpect(status().isForbidden());
                mvc.perform(get("/requests/1/delete").with(user("test").roles(role)))
                        .andExpect(status().isForbidden());
                mvc.perform(post("/requests/1/delete").with(user("test").roles(role)).with(csrf()))
                        .andExpect(status().isForbidden());
                mvc.perform(get("/requests/1/edit").with(user("test").roles(role)))
                        .andExpect(status().isForbidden());
                mvc.perform(post("/requests/1").with(user("test").roles(role)).with(csrf()))
                        .andExpect(status().isForbidden());
            }
            if (!role.equals("SYSTEM_ADMIN")) {
                mvc.perform(post("/requests/1/review").with(user("test").roles(role)).with(csrf()).param("approved", "true"))
                        .andExpect(status().isForbidden());
            }
        }
        mvc.perform(get("/requests").with(user("volunteer").roles("VOLUNTEER")))
                .andExpect(status().isForbidden());
        org.mockito.Mockito.verifyNoInteractions(requestService, verificationService, userRepository);
        mvc.perform(post("/requests/1/review").with(user("admin").roles("SYSTEM_ADMIN")).with(csrf()).param("approved", "true"))
                .andExpect(redirectedUrl("/requests#verification"));
        org.mockito.Mockito.verify(verificationService).reviewRequest(org.mockito.ArgumentMatchers.eq(1L), any());
    }

    @Test
    void missingOrInvalidCsrfTokensCannotChangeDataOrLogOut() throws Exception {
        var session = new org.springframework.mock.web.MockHttpSession();
        mvc.perform(get("/requests/new").session(session).with(user("test").roles("APPLICANT")))
                .andExpect(status().isOk());
        org.mockito.Mockito.clearInvocations(requestService);
        for (String path : new String[]{"/requests", "/requests/1/review", "/logout"}) {
            String role = path.contains("review") ? "SYSTEM_ADMIN" : "APPLICANT";
            mvc.perform(post(path).session(session).with(user("test").roles(role)).param("approved", "true"))
                    .andExpect(status().isForbidden());
            mvc.perform(post(path).session(session).with(user("test").roles(role)).with(csrf().useInvalidToken()).param("approved", "true"))
                    .andExpect(status().isForbidden());
        }
        assertThat(session.isInvalid()).isFalse();
        org.mockito.Mockito.verifyNoInteractions(requestService, verificationService, userRepository);
    }

    @Test
    void applicantOnlyReadsAndCreatesRequestsUnderOwnIdentity() throws Exception {
        var applicant = new com.example.help_bridge.fundraising.user.entity.User();
        org.springframework.test.util.ReflectionTestUtils.setField(applicant, "id", 7L);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(java.util.Optional.of(applicant));
        mvc.perform(get("/requests").with(user("APPLICANT:applicant@test.com").roles("APPLICANT"))
                        .param("userId", "99").param("requesterId", "99"))
                .andExpect(status().isOk());
        org.mockito.Mockito.verify(requestService).getRequestsByUser(7L);
        org.mockito.Mockito.verify(requestService, org.mockito.Mockito.never()).getSharedPool();
        var created = org.mockito.Mockito.mock(com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse.class);
        when(created.id()).thenReturn(42L);
        when(requestService.createRequest(any())).thenReturn(created);
        mvc.perform(post("/requests").with(user("APPLICANT:applicant@test.com").roles("APPLICANT")).with(csrf())
                        .param("userId", "99").param("requesterId", "99")
                        .param("assistanceType", "MEDICAL").param("amount", "100")
                        .param("deadline", java.time.LocalDate.now().plusDays(10).toString())
                        .param("situationDescription", "Потрібна допомога").param("needDescription", "Лікування")
                        .param("institutionName", "Лікарня").param("applicationNumber", "DOC-1")
                        .param("dataProcessingConsent", "true"))
                .andExpect(redirectedUrl("/requests/42"));
        org.mockito.Mockito.verify(requestService).createRequest(org.mockito.ArgumentMatchers.argThat(dto -> dto.userId().equals(7L)));
    }

    @Test
    void menuOnlyLinksToAllowedSectionsAndPrivatePagesAreNotCached() throws Exception {
        var applicant = new com.example.help_bridge.fundraising.user.entity.User();
        org.springframework.test.util.ReflectionTestUtils.setField(applicant, "id", 7L);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(java.util.Optional.of(applicant));
        for (String role : new String[]{"APPLICANT", "FUND_REPRESENTATIVE", "SYSTEM_ADMIN", "VOLUNTEER"}) {
            String path = role.equals("VOLUNTEER") ? "/home" : "/requests";
            var result = mvc.perform(get(path).with(user("APPLICANT:applicant@test.com").roles(role)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                    .andReturn();
            String html = result.getResponse().getContentAsString();
            String nav = html.substring(html.indexOf("<nav"), html.indexOf("</nav>"));
            assertThat(nav.contains("href=\"/requests/new\"")).isEqualTo(role.equals("APPLICANT"));
            assertThat(nav.contains("href=\"/requests#verification\"")).isEqualTo(role.equals("SYSTEM_ADMIN"));
            if (role.equals("VOLUNTEER")) assertThat(nav).doesNotContain("href=\"/requests\"");
        }
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/css/app.css")).andExpect(status().isOk());
        mvc.perform(get("/js/forms.js")).andExpect(status().isOk());
    }

    @Test
    void loginRotatesSessionAndLogoutRemovesAccess() throws Exception {
        var principal = User.withUsername("VOLUNTEER:vera@test.com").password("hash").roles("VOLUNTEER").build();
        when(authenticationManager.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        var session = new org.springframework.mock.web.MockHttpSession();
        String oldId = session.getId();
        mvc.perform(post("/login").session(session).with(csrf()).param("username", principal.getUsername()).param("password", "secret"))
                .andExpect(redirectedUrl("/home"));
        assertThat(session.getId()).isNotEqualTo(oldId);
        mvc.perform(get("/home").session(session)).andExpect(status().isOk());
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/home")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/home").with(request -> {
            request.setRequestedSessionId(session.getId());
            request.setRequestedSessionIdValid(false);
            return request;
        })).andExpect(redirectedUrl("/login?expired"));
    }
}
