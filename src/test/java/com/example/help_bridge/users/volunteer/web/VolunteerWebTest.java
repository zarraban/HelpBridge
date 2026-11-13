package com.example.help_bridge.users.volunteer.web;

import com.example.help_bridge.common.config.SecurityConfiguration;
import com.example.help_bridge.common.security.service.*;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerResponse;
import com.example.help_bridge.users.volunteer.exception.*;
import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
import com.example.help_bridge.users.volunteer.service.VolunteerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VolunteerWebController.class)
@Import(SecurityConfiguration.class)
class VolunteerWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean VolunteerService volunteers;
    @MockitoBean VolunteerAccessPolicy access;
    @MockitoBean RequestAccessPolicy identity;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean JwtService jwtService;
    @MockitoBean JwtValidatorService jwtValidatorService;
    @MockitoBean AuthenticationEntryPoint entryPoint;
    @MockitoBean AccessDeniedHandler deniedHandler;
    static final String BASE = "/funds/2/volunteers";
    VolunteerResponse volunteer() { return new VolunteerResponse(5L, 2L, "Олена", "Коваль", "olena@test.org", "+380501234567"); }

    @Test void listEmptyStateAndPopulatedCardsRender() throws Exception {
        when(volunteers.getFundVolunteers(2L)).thenReturn(List.of());
        mvc.perform(get(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("У команді ще немає волонтерів")));
        when(volunteers.getFundVolunteers(2L)).thenReturn(List.of(volunteer()));
        mvc.perform(get(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Олена Коваль")))
                .andExpect(content().string(containsString(BASE + "/5/edit")));
    }
    @Test void teamShortcutUsesAuthenticatedFund() throws Exception {
        when(identity.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("FUND_REPRESENTATIVE", null, 2L));
        mvc.perform(get("/team").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(redirectedUrl(BASE));
    }
    @Test void formsRenderWithoutExposingPasswordOrOfferingPasswordEdit() throws Exception {
        mvc.perform(get(BASE + "/new").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"password\"")));
        when(volunteers.getFundVolunteer(2L, 5L)).thenReturn(volunteer());
        mvc.perform(get(BASE + "/5/edit").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("olena@test.org")))
                .andExpect(content().string(not(containsString("name=\"password\""))));
        mvc.perform(get(BASE + "/5/delete").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Історію призначень буде збережено")));
    }
    @Test void createsAndUpdatesWithRedirect() throws Exception {
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf())
                .param("firstName", "Олена").param("lastName", "Коваль").param("email", "olena@test.org")
                .param("phone", "+380501234567").param("password", "Secret123!"))
                .andExpect(redirectedUrl(BASE));
        verify(volunteers).registerVolunteer(argThat(c -> c.fundId().equals(2L) && c.password().equals("Secret123!")));
        when(volunteers.getFundVolunteer(2L, 5L)).thenReturn(volunteer());
        mvc.perform(post(BASE + "/5").with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf())
                .param("firstName", "Олена").param("lastName", "Коваль").param("email", "olena@test.org").param("phone", "+380501234567"))
                .andExpect(redirectedUrl(BASE));
        verify(volunteers).updateVolunteer(argThat(c -> c.id().equals(5L) && c.fundId().equals(2L)));
    }
    @Test void invalidFormPreservesContactsButDoesNotEchoPassword() throws Exception {
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf())
                .param("firstName", "Олена").param("email", "olena@test.org").param("phone", "bad").param("password", "Secret123!"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "lastName", "phone"))
                .andExpect(content().string(containsString("olena@test.org")))
                .andExpect(content().string(not(containsString("Secret123!"))));
        verifyNoInteractions(volunteers);
    }
    @Test void duplicateContactsShowConflictAndRetainForm() throws Exception {
        when(volunteers.registerVolunteer(any())).thenThrow(new DuplicateVolunteerException("duplicate"));
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf())
                .param("firstName", "Олена").param("lastName", "Коваль").param("email", "olena@test.org")
                .param("phone", "+380501234567").param("password", "Secret123!"))
                .andExpect(status().isConflict()).andExpect(content().string(containsString("Email або телефон")))
                .andExpect(content().string(not(containsString("Secret123!"))));
    }
    @Test void deletionAndBlockedOrRepeatedDeletionHaveClearMessages() throws Exception {
        mvc.perform(post(BASE + "/5/delete").with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attributeExists("message"));
        doThrow(new VolunteerHasActiveAssignmentsException()).when(volunteers).removeVolunteer(2L, 5L);
        mvc.perform(post(BASE + "/5/delete").with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attribute("error", containsString("активні призначення")));
        doThrow(new VolunteerNotFoundException("missing")).when(volunteers).removeVolunteer(2L, 5L);
        mvc.perform(post(BASE + "/5/delete").with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attributeExists("error"));
    }
    @Test void guestWrongRolesCsrfAndForeignFundAreDenied() throws Exception {
        mvc.perform(get(BASE)).andExpect(redirectedUrl("/login"));
        for (String role : List.of("APPLICANT", "VOLUNTEER", "DONOR")) {
            mvc.perform(get(BASE).with(user("user").roles(role)).accept("text/html")).andExpect(status().isForbidden());
            mvc.perform(post(BASE + "/5/delete").with(user("user").roles(role)).with(csrf()).accept("text/html"))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(post(BASE + "/5/delete").with(user("rep").roles("FUND_REPRESENTATIVE"))).andExpect(redirectedUrl("/login?expired"));
        doThrow(new AccessDeniedException("foreign fund")).when(access).requireManageFund(3L);
        mvc.perform(get("/funds/3/volunteers/new").with(user("rep").roles("FUND_REPRESENTATIVE")).accept("text/html"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(volunteers);
    }
    @Test void missingVolunteerRenders404() throws Exception {
        when(volunteers.getFundVolunteer(2L, 5L)).thenThrow(new VolunteerNotFoundException("missing"));
        mvc.perform(get(BASE + "/5/edit").with(user("rep").roles("FUND_REPRESENTATIVE")).accept("text/html"))
                .andExpect(status().isNotFound()).andExpect(view().name("error/404"));
    }
}
