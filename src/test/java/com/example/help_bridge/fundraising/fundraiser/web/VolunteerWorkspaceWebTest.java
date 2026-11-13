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

@WebMvcTest({VolunteerWorkspaceWebController.class, LoginController.class})
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
}
