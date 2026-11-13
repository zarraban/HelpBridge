package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.common.config.SecurityConfiguration;
import com.example.help_bridge.common.security.service.*;
import com.example.help_bridge.common.web.LoginController;
import com.example.help_bridge.fundraising.fundraiser.dto.response.*;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.service.*;
import com.example.help_bridge.fundraising.request.entity.*;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.users.volunteer.entity.*;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
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

@WebMvcTest({VolunteerWorkspaceWebController.class, VolunteerAssignmentActionWebController.class, LoginController.class})
@Import({SecurityConfiguration.class, VolunteerWorkspaceService.class, VolunteerAccessPolicy.class})
class VolunteerWorkspaceWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean FundraiserService fundraisers;
    @MockitoBean FundraiserAssignmentService assignments;
    @MockitoBean RequestRepository requests;
    @MockitoBean VolunteerRepository volunteers;
    @MockitoBean RequestAccessPolicy identity;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean JwtService jwtService;
    @MockitoBean JwtValidatorService jwtValidatorService;
    @MockitoBean AuthenticationEntryPoint entryPoint;
    @MockitoBean AccessDeniedHandler deniedHandler;
    final LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 0);

    Volunteer activeVolunteer() {
        var volunteer = new Volunteer(); volunteer.setId(5L); volunteer.setStatus(VolunteerStatus.ACTIVE);
        when(volunteers.findByEmail("vera@test.org")).thenReturn(java.util.Optional.of(volunteer));
        return volunteer;
    }
    FundraiserAssignmentResponse assignment(Long id, AssignmentStatus status) {
        return new FundraiserAssignmentResponse(id, 5L, status, now, status == AssignmentStatus.ACTIVE ? null : now,
                status == AssignmentStatus.RETURNED ? "Не можу продовжити" : null,
                10L, "Збір №10", FundraiserStatus.IN_PROGRESS, status == AssignmentStatus.COMPLETED ? "Доставлено <script>alert(1)</script>" : null);
    }
    void fundraiser(FundraiserStatus status) {
        when(fundraisers.getFundraiserById(10L)).thenReturn(new FundraiserResponse(10L, 20L, status, now, null, List.of()));
    }
    @Test void emptyListsAndMenuRender() throws Exception {
        activeVolunteer();
        mvc.perform(get("/my/assignments").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Активних призначень поки немає")))
                .andExpect(content().string(containsString("Завершених або повернутих")))
                .andExpect(content().string(containsString("aria-current=\"page\">Мої призначення")));
    }
    @Test void currentIdentityControlsListAndResultsAreGroupedAndSorted() throws Exception {
        activeVolunteer();
        var older = assignment(30L, AssignmentStatus.ACTIVE);
        var newer = assignment(31L, AssignmentStatus.ACTIVE);
        var completed = assignment(32L, AssignmentStatus.COMPLETED);
        var returned = assignment(33L, AssignmentStatus.RETURNED);
        when(assignments.getVolunteerAssignments(5L)).thenReturn(List.of(older, newer, completed, returned));
        mvc.perform(get("/my/assignments?volunteerId=99").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(model().attribute("activeAssignments", List.of(newer, older)))
                .andExpect(model().attribute("pastAssignments", List.of(returned, completed)))
                .andExpect(content().string(containsString("/my/fundraisers/10")))
                .andExpect(content().string(containsString("Не можу продовжити")))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
        verify(assignments, never()).getVolunteerAssignments(99L);
    }
    @Test void assignedFundraiserShowsOnlyWorkSummaryAndOwnHistory() throws Exception {
        activeVolunteer();
        when(assignments.getVolunteerAssignments(5L)).thenReturn(List.of(assignment(30L, AssignmentStatus.ACTIVE)));
        fundraiser(FundraiserStatus.IN_PROGRESS);
        var request = new Request(new User("SECRET_NAME", "SECRET_LAST_NAME", "SECRET_EMAIL", "hash"),
                new AssistanceType("MEDICAL", "Лікування", 1), BigDecimal.TEN, LocalDate.of(2027, 1, 1),
                "SECRET_SITUATION", "Доставити допомогу", "Клініка", "SECRET_DOC_NUMBER", true);
        when(requests.findById(20L)).thenReturn(java.util.Optional.of(request));
        mvc.perform(get("/my/fundraisers/10").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Доставити допомогу")))
                .andExpect(content().string(containsString("Лікування")))
                .andExpect(content().string(containsString("01.01.2027")))
                .andExpect(content().string(not(containsString("SECRET_"))))
                .andExpect(content().string(not(containsString("/requests/"))))
                .andExpect(content().string(not(containsString("Закрити збір"))))
                .andExpect(content().string(not(containsString("Призначити волонтера"))));
        verify(assignments, never()).getFundraiserAssignments(any());
    }
    @Test void unassignedFundraiserIsDeniedBeforeReadingFundraiserOrRequest() throws Exception {
        activeVolunteer();
        when(assignments.getVolunteerAssignments(5L)).thenReturn(List.of(assignment(30L, AssignmentStatus.ACTIVE)));
        mvc.perform(get("/my/fundraisers/99").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).accept("text/html"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(fundraisers, requests);
    }
    @Test void inactiveVolunteerCannotReadListOrDetails() throws Exception {
        activeVolunteer().setStatus(VolunteerStatus.INACTIVE);
        for (String path : List.of("/my/assignments", "/my/fundraisers/10")) {
            mvc.perform(get(path).with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).accept("text/html"))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(assignments, fundraisers, requests);
    }
    @Test void historicalAssignmentCanReadClosedFundraiserWithMissingRequest() throws Exception {
        activeVolunteer();
        when(assignments.getVolunteerAssignments(5L)).thenReturn(List.of(assignment(30L, AssignmentStatus.COMPLETED)));
        fundraiser(FundraiserStatus.CLOSED);
        mvc.perform(get("/my/fundraisers/10").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Закритий")))
                .andExpect(content().string(containsString("Інформація про пов’язану заявку зараз недоступна")))
                .andExpect(content().string(containsString("Завершено")));
    }
    @Test void missingAssignedFundraiserRenders404() throws Exception {
        activeVolunteer();
        when(assignments.getVolunteerAssignments(5L)).thenReturn(List.of(assignment(30L, AssignmentStatus.ACTIVE)));
        when(fundraisers.getFundraiserById(10L)).thenThrow(new FundraiserNotFoundException("missing"));
        mvc.perform(get("/my/fundraisers/10").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).accept("text/html"))
                .andExpect(status().isNotFound()).andExpect(view().name("error/404"));
    }
    @Test void guestsAndOtherRolesCannotOpenWorkspace() throws Exception {
        for (String path : List.of("/my/assignments", "/my/fundraisers/10")) {
            mvc.perform(get(path)).andExpect(redirectedUrl("/login"));
            for (String role : List.of("APPLICANT", "FUND_REPRESENTATIVE", "SYSTEM_ADMIN", "DONOR")) {
                mvc.perform(get(path).with(user("other").roles(role)).accept("text/html"))
                        .andExpect(status().isForbidden());
            }
        }
        verifyNoInteractions(assignments, fundraisers, requests, volunteers);
    }
    @Test void volunteerHomeLinksToAssignments() throws Exception {
        mvc.perform(get("/home").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("href=\"/my/assignments\"")))
                .andExpect(content().string(not(containsString("Розділи ваших призначень"))));
    }
    void ownAssignment(AssignmentStatus status) {
        activeVolunteer();
        when(assignments.getVolunteerAssignments(5L)).thenReturn(List.of(assignment(30L, status)));
    }
    @Test void activeAssignmentHasActionLinksAndBothFormsRender() throws Exception {
        ownAssignment(AssignmentStatus.ACTIVE);
        mvc.perform(get("/my/assignments").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(content().string(containsString("/my/assignments/30/complete")))
                .andExpect(content().string(containsString("/my/assignments/30/return")));
        mvc.perform(get("/my/assignments/30/complete").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"closingComment\"")))
                .andExpect(content().string(containsString("не закриває збір")));
        mvc.perform(get("/my/assignments/30/return").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"returnReason\"")));
    }
    @Test void validCompletionAndReturnPassCommentsAndRedirectToUpdatedList() throws Exception {
        ownAssignment(AssignmentStatus.ACTIVE);
        mvc.perform(post("/my/assignments/30/complete").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).with(csrf())
                        .param("closingComment", "Допомогу доставлено"))
                .andExpect(redirectedUrl("/my/assignments")).andExpect(flash().attributeExists("message"));
        verify(assignments).completeAssignment(30L, new com.example.help_bridge.fundraising.fundraiser.dto.request.CompleteAssignmentRequest("Допомогу доставлено"));
        mvc.perform(post("/my/assignments/30/return").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).with(csrf())
                        .param("returnReason", "Не можу продовжити"))
                .andExpect(redirectedUrl("/my/assignments")).andExpect(flash().attributeExists("message"));
        verify(assignments).returnAssignment(30L, new com.example.help_bridge.fundraising.fundraiser.dto.request.ReturnAssignmentRequest("Не можу продовжити"));
    }
    @Test void invalidCommentsAreRejectedAndTextIsRetained() throws Exception {
        ownAssignment(AssignmentStatus.ACTIVE);
        for (String action : List.of("complete", "return")) {
            String field = action.equals("complete") ? "closingComment" : "returnReason";
            for (String comment : List.of("", "   ", "abc", "я".repeat(501))) {
                mvc.perform(post("/my/assignments/30/" + action).with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).with(csrf())
                                .param(field, comment))
                        .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", field))
                        .andExpect(content().string(containsString(comment)));
            }
        }
        verify(assignments, never()).completeAssignment(any(), any());
        verify(assignments, never()).returnAssignment(any(), any());
    }
    @Test void nonActiveAssignmentsHideActionsAndRejectRepeatedPosts() throws Exception {
        for (AssignmentStatus state : List.of(AssignmentStatus.COMPLETED, AssignmentStatus.RETURNED)) {
            ownAssignment(state);
            mvc.perform(get("/my/assignments").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                    .andExpect(content().string(not(containsString("/my/assignments/30/complete"))))
                    .andExpect(content().string(not(containsString("/my/assignments/30/return"))));
            for (String action : List.of("complete", "return")) {
                mvc.perform(get("/my/assignments/30/" + action).with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                        .andExpect(status().isConflict()).andExpect(model().attribute("actionable", false));
                mvc.perform(post("/my/assignments/30/" + action).with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).with(csrf())
                                .param("closingComment", "Допомогу доставлено").param("returnReason", "Не можу продовжити"))
                        .andExpect(status().isConflict()).andExpect(model().attribute("actionable", false));
            }
        }
        verify(assignments, never()).completeAssignment(any(), any());
        verify(assignments, never()).returnAssignment(any(), any());
    }
    @Test void staleFormConflictPreservesCommentAndDisablesSubmission() throws Exception {
        ownAssignment(AssignmentStatus.ACTIVE);
        when(assignments.completeAssignment(any(), any())).thenThrow(new com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException("changed"));
        mvc.perform(post("/my/assignments/30/complete").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).with(csrf())
                        .param("closingComment", "Допомогу доставлено"))
                .andExpect(status().isConflict()).andExpect(model().attribute("actionable", false))
                .andExpect(content().string(containsString("Допомогу доставлено")))
                .andExpect(content().string(not(containsString("Підтвердити завершення</button>"))));
    }
    @Test void missingAssignmentDuringSaveExplainsFailure() throws Exception {
        ownAssignment(AssignmentStatus.ACTIVE);
        when(assignments.returnAssignment(any(), any())).thenThrow(new com.example.help_bridge.fundraising.fundraiser.exception.AssignmentNotFoundException("gone"));
        mvc.perform(post("/my/assignments/30/return").with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).with(csrf())
                        .param("returnReason", "Не можу продовжити"))
                .andExpect(redirectedUrl("/my/assignments")).andExpect(flash().attributeExists("error"));
    }
    @Test void foreignAssignmentActionsAreDeniedBeforeMutation() throws Exception {
        ownAssignment(AssignmentStatus.ACTIVE);
        for (String action : List.of("complete", "return")) {
            mvc.perform(get("/my/assignments/99/" + action).with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).accept("text/html"))
                    .andExpect(status().isForbidden());
            mvc.perform(post("/my/assignments/99/" + action).with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")).with(csrf()).accept("text/html")
                            .param("closingComment", "Допомогу доставлено").param("returnReason", "Не можу продовжити"))
                    .andExpect(status().isForbidden());
        }
        verify(assignments, never()).completeAssignment(any(), any());
        verify(assignments, never()).returnAssignment(any(), any());
    }
    @Test void actionPostsRequireVolunteerRoleAndCsrf() throws Exception {
        for (String action : List.of("complete", "return")) {
            for (String role : List.of("FUND_REPRESENTATIVE", "SYSTEM_ADMIN", "DONOR", "APPLICANT")) {
                mvc.perform(post("/my/assignments/30/" + action).with(user("other").roles(role)).with(csrf()).accept("text/html"))
                        .andExpect(status().isForbidden());
            }
            mvc.perform(post("/my/assignments/30/" + action).with(user("VOLUNTEER:vera@test.org").roles("VOLUNTEER")))
                    .andExpect(redirectedUrl("/login?expired"));
        }
        verifyNoInteractions(assignments, volunteers);
    }

}
