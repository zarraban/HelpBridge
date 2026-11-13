package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.common.config.SecurityConfiguration;
import com.example.help_bridge.common.security.service.*;
import com.example.help_bridge.fundraising.fundraiser.dto.response.*;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException;
import com.example.help_bridge.fundraising.fundraiser.service.*;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.service.RequestService;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerResponse;
import com.example.help_bridge.users.volunteer.service.VolunteerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({FundraiserWebController.class, FundraiserAssignmentWebController.class})
@Import(SecurityConfiguration.class)
class FundraiserAssignmentWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean FundraiserService fundraisers;
    @MockitoBean FundraiserAssignmentService assignments;
    @MockitoBean RequestService requests;
    @MockitoBean VolunteerService volunteers;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean JwtService jwtService;
    @MockitoBean JwtValidatorService jwtValidatorService;
    @MockitoBean AuthenticationEntryPoint entryPoint;
    @MockitoBean AccessDeniedHandler deniedHandler;
    static final String BASE = "/fundraisers/10/assignments";
    final LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 0);

    void setup(FundraiserStatus status) {
        when(fundraisers.getFundraiserById(10L)).thenReturn(new FundraiserResponse(10L, 20L, status, now, null, List.of()));
        when(requests.getRequestById(20L)).thenReturn(new RequestResponse(20L, 1L, 2L, "MEDICAL", BigDecimal.TEN,
                LocalDate.of(2027, 1, 1), "Situation", "Need", "Clinic", "DOC", RequestStatus.IN_PROGRESS, false, now, List.of()));
        when(volunteers.getFundVolunteers(2L)).thenReturn(List.of(new VolunteerResponse(5L, 2L, "Олена", "Коваль", "olena@test.org", "+380501234567")));
    }
    FundraiserAssignmentResponse history(AssignmentStatus status) {
        return new FundraiserAssignmentResponse(30L, 5L, status, now, status == AssignmentStatus.ACTIVE ? null : now,
                status == AssignmentStatus.RETURNED ? "Не можу продовжити" : null, 10L, "Збір №10", FundraiserStatus.IN_PROGRESS,
                status == AssignmentStatus.COMPLETED ? "Допомогу доставлено" : null);
    }
    @Test void emptyDetailsRenderLinkToAssignmentForm() throws Exception {
        setup(FundraiserStatus.PENDING_ASSIGNMENT);
        mvc.perform(get("/fundraisers/10").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("На цей збір ще не призначали волонтерів")))
                .andExpect(content().string(containsString(BASE + "/new")));
    }
    @Test void historyDisplaysAllStatusesDatesAndCommentsSafely() throws Exception {
        setup(FundraiserStatus.IN_PROGRESS);
        when(assignments.getFundraiserAssignments(10L)).thenReturn(List.of(history(AssignmentStatus.ACTIVE), history(AssignmentStatus.RETURNED), history(AssignmentStatus.COMPLETED)));
        mvc.perform(get("/fundraisers/10").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Олена Коваль")))
                .andExpect(content().string(containsString("Активне"))).andExpect(content().string(containsString("Повернуто")))
                .andExpect(content().string(containsString("Завершено"))).andExpect(content().string(containsString("13.11.2026 10:00")))
                .andExpect(content().string(containsString("Не можу продовжити"))).andExpect(content().string(containsString("Допомогу доставлено")));
    }
    @Test void onlyEligibleVolunteersAreOfferedAndHistoricalAssignmentsCanBeAssignedAgain() throws Exception {
        setup(FundraiserStatus.IN_PROGRESS);
        when(assignments.getFundraiserAssignments(10L)).thenReturn(List.of(history(AssignmentStatus.ACTIVE)));
        mvc.perform(get(BASE + "/new").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(model().attribute("availableVolunteers", empty()))
                .andExpect(content().string(not(containsString("type=\"submit\" class=\"btn\""))));
        when(assignments.getFundraiserAssignments(10L)).thenReturn(List.of(history(AssignmentStatus.RETURNED)));
        mvc.perform(get(BASE + "/new").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(model().attribute("availableVolunteers", hasSize(1)))
                .andExpect(content().string(containsString("Олена Коваль")));
    }
    @Test void assignmentRedirectsToHistory() throws Exception {
        setup(FundraiserStatus.PENDING_ASSIGNMENT);
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()).param("volunteerId", "5"))
                .andExpect(redirectedUrl("/fundraisers/10#assignments")).andExpect(flash().attributeExists("message"));
        verify(assignments).assignVolunteerToFund(10L, new com.example.help_bridge.fundraising.fundraiser.dto.request.AssignVolunteerRequest(5L));
    }
    @Test void missingOrForgedSelectionDoesNotCreateAssignment() throws Exception {
        setup(FundraiserStatus.IN_PROGRESS);
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "volunteerId"));
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()).param("volunteerId", "99"))
                .andExpect(status().isConflict()).andExpect(model().attributeHasFieldErrors("form", "volunteerId"));
        verify(assignments, never()).assignVolunteerToFund(any(), any());
    }
    @Test void closedFundraiserHasNoAssignmentActionAndRejectsPost() throws Exception {
        setup(FundraiserStatus.CLOSED);
        mvc.perform(get("/fundraisers/10").with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString(BASE + "/new"))));
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()).param("volunteerId", "5"))
                .andExpect(status().isConflict()).andExpect(content().string(containsString("Збір закрито")));
        verify(assignments, never()).assignVolunteerToFund(any(), any());
    }
    @Test void duplicateAfterFormWasOpenedShowsConflict() throws Exception {
        setup(FundraiserStatus.IN_PROGRESS);
        when(assignments.assignVolunteerToFund(any(), any())).thenThrow(new InvalidAssignmentStateException("duplicate"));
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()).param("volunteerId", "5"))
                .andExpect(status().isConflict()).andExpect(content().string(containsString("Призначення не виконано")));
    }
    @Test void foreignFundGuestWrongRoleAndCsrfAreProtected() throws Exception {
        mvc.perform(get(BASE + "/new")).andExpect(redirectedUrl("/login"));
        mvc.perform(get(BASE + "/new").with(user("volunteer").roles("VOLUNTEER")).accept("text/html"))
                .andExpect(status().isForbidden());
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")))
                .andExpect(redirectedUrl("/login?expired"));
        when(fundraisers.getFundraiserById(10L)).thenThrow(new AccessDeniedException("foreign fund"));
        mvc.perform(post(BASE).with(user("rep").roles("FUND_REPRESENTATIVE")).with(csrf()).param("volunteerId", "5").accept("text/html"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(volunteers, assignments);
    }
}
