package com.example.help_bridge.fundraising.fundraiser.security;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.repository.*;
import com.example.help_bridge.fundraising.fundraiser.service.impl.*;
import com.example.help_bridge.fundraising.fundraiser.spi.VolunteerDirectory;
import com.example.help_bridge.fundraising.fundraiser.dto.request.*;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.users.volunteer.entity.*;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
import com.example.help_bridge.users.volunteer.service.VolunteerServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteerWorkflowAccessTest {
    @Mock RequestAccessPolicy identity;
    @Mock RequestRepository requests;
    @Mock VolunteerRepository volunteers;
    @Mock FundraiserAssignmentRepository assignments;
    @Mock FundraiserRepository fundraisers;
    @Mock EvidenceRepository evidences;
    @Mock VolunteerDirectory directory;
    VolunteerAccessPolicy team;
    FundraiserAccessPolicy access;
    Fundraiser fundraiser;
    FundraiserAssignment assignment;
    FundraiserAssignmentServiceImpl service;

    @BeforeEach void setUp() {
        team = new VolunteerAccessPolicy(identity, volunteers);
        access = new FundraiserAccessPolicy(identity, team, assignments, requests);
        service = new FundraiserAssignmentServiceImpl(assignments, fundraisers, directory, access, team);
        fundraiser = new Fundraiser();
        fundraiser.setId(10L);
        fundraiser.setRequestId(20L);
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        assignment = new FundraiserAssignment();
        assignment.setId(30L);
        assignment.setFundraiser(fundraiser);
        assignment.setVolunteerId(5L);
        assignment.setStatus(AssignmentStatus.ACTIVE);
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    void volunteer() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "VOLUNTEER:volunteer@test.org", "", List.of(new SimpleGrantedAuthority("ROLE_VOLUNTEER"))));
        Volunteer volunteer = new Volunteer();
        volunteer.setId(5L);
        volunteer.setStatus(VolunteerStatus.ACTIVE);
        when(volunteers.findByEmail("volunteer@test.org")).thenReturn(Optional.of(volunteer));
    }
    void representative() {
        when(identity.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("FUND_REPRESENTATIVE", null, 2L));
    }

    @Test void representativeCannotReadOrMutateForeignTeam() {
        representative();
        var service = new VolunteerServiceImpl(volunteers, mock(org.springframework.context.ApplicationEventPublisher.class),
                mock(com.example.help_bridge.fundraising.fund.repository.FundRepository.class),
                mock(org.springframework.security.crypto.password.PasswordEncoder.class), team);
        assertThrows(AccessDeniedException.class, () -> service.getFundVolunteers(3L));
        assertThrows(AccessDeniedException.class, () -> service.getFundVolunteer(3L, 5L));
        assertThrows(AccessDeniedException.class, () -> service.removeVolunteer(3L, 5L));
        verifyNoInteractions(volunteers);
    }
    @Test void ownTeamAndAdminAreAllowed() {
        representative();
        assertDoesNotThrow(() -> team.requireManageFund(2L));
        when(identity.currentViewer()).thenReturn(new RequestAccessPolicy.Viewer("SYSTEM_ADMIN", null, null));
        assertDoesNotThrow(() -> team.requireManageFund(3L));
    }
    @Test void volunteerOnlyReadsOwnAssignmentList() {
        volunteer();
        when(assignments.findByVolunteerId(5L)).thenReturn(List.of(assignment));
        assertEquals(1, service.getVolunteerAssignments(5L).size());
        assertThrows(AccessDeniedException.class, () -> service.getVolunteerAssignments(6L));
        verify(assignments, never()).findByVolunteerId(6L);
    }
    @Test void foreignAssignmentCannotBeCompletedOrReturned() {
        volunteer();
        assignment.setVolunteerId(6L);
        when(assignments.findById(30L)).thenReturn(Optional.of(assignment));
        assertThrows(AccessDeniedException.class, () -> service.completeAssignment(30L, new CompleteAssignmentRequest("Completed work")));
        assertThrows(AccessDeniedException.class, () -> service.returnAssignment(30L, new ReturnAssignmentRequest("Cannot continue")));
        assertEquals(AssignmentStatus.ACTIVE, assignment.getStatus());
        verify(assignments, never()).save(any());
    }
    @Test void ownActiveAssignmentCanBeCompleted() {
        volunteer();
        when(assignments.findById(30L)).thenReturn(Optional.of(assignment));
        when(fundraisers.findById(10L)).thenReturn(Optional.of(fundraiser));
        service.completeAssignment(30L, new CompleteAssignmentRequest("Completed work"));
        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
    }
    @Test void historicalAssignmentAllowsViewingButNotAddingEvidence() {
        volunteer();
        assignment.setStatus(AssignmentStatus.COMPLETED);
        when(assignments.findByFundraiserId(10L)).thenReturn(List.of(assignment));
        assertDoesNotThrow(() -> access.requireView(fundraiser));
        when(fundraisers.findById(10L)).thenReturn(Optional.of(fundraiser));
        var service = new EvidenceServiceImpl(evidences, fundraisers, List.of(), access);
        assertThrows(AccessDeniedException.class, () -> service.addEvidenceToFundraiser(10L,
                new AddEvidenceRequest("receipt", null, null)));
        verify(evidences, never()).save(any());
    }
    @Test void activeAssignmentAllowsEvidenceButNotManagement() {
        volunteer();
        when(assignments.existsByFundraiserIdAndVolunteerIdAndStatus(10L, 5L, AssignmentStatus.ACTIVE)).thenReturn(true);
        assertDoesNotThrow(() -> access.requireAddEvidence(fundraiser));
        when(identity.currentViewer()).thenThrow(new AccessDeniedException("Volunteer cannot manage fundraisers"));
        assertThrows(AccessDeniedException.class, () -> access.requireRepresentative(fundraiser));
    }
    @Test void unassignedFundraiserCannotBeViewed() {
        volunteer();
        assertThrows(AccessDeniedException.class, () -> access.requireView(fundraiser));
    }
    @Test void representativeCannotAssignVolunteerFromAnotherFund() {
        representative();
        when(requests.existsByIdAndFundId(20L, 2L)).thenReturn(true);
        Fund fund = new Fund(); fund.setId(2L);
        Request request = mock(Request.class); when(request.getFund()).thenReturn(fund);
        when(requests.findById(20L)).thenReturn(Optional.of(request));
        when(fundraisers.findById(10L)).thenReturn(Optional.of(fundraiser));
        assertThrows(AccessDeniedException.class, () -> service.assignVolunteerToFund(10L, new AssignVolunteerRequest(6L)));
        verify(assignments, never()).save(any());
    }
    @Test void representativeCannotCompleteForeignFundsAssignment() {
        representative();
        when(assignments.findById(30L)).thenReturn(Optional.of(assignment));
        assertThrows(AccessDeniedException.class, () -> service.completeAssignment(30L, new CompleteAssignmentRequest("Completed work")));
        verify(assignments, never()).save(any());
    }
    @Test void inactiveVolunteerCannotReadAssignments() {
        volunteer();
        Volunteer inactive = new Volunteer(); inactive.setId(5L); inactive.setStatus(VolunteerStatus.INACTIVE);
        when(volunteers.findByEmail("volunteer@test.org")).thenReturn(Optional.of(inactive));
        assertThrows(AccessDeniedException.class, () -> service.getVolunteerAssignments(5L));
        verifyNoInteractions(assignments);
    }
    @Test void ownAssignmentCanBeReturnedAndOnlyLastReturnResetsFundraiser() {
        volunteer();
        when(assignments.findById(30L)).thenReturn(Optional.of(assignment));
        service.returnAssignment(30L, new ReturnAssignmentRequest("Cannot continue"));
        assertEquals(AssignmentStatus.RETURNED, assignment.getStatus());
        assertEquals(FundraiserStatus.PENDING_ASSIGNMENT, fundraiser.getStatus());
        assertThrows(com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException.class,
                () -> service.returnAssignment(30L, new ReturnAssignmentRequest("Cannot continue")));
    }
    @Test void anonymousTeamAccessIsDenied() {
        var actualIdentity = new RequestAccessPolicy(mock(com.example.help_bridge.fundraising.user.repository.UserRepository.class),
                mock(com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository.class));
        assertThrows(AccessDeniedException.class, () -> new VolunteerAccessPolicy(actualIdentity, volunteers).requireManageFund(2L));
        assertThrows(AccessDeniedException.class, () -> team.currentVolunteerId());
    }
}
