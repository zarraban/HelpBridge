package com.example.help_bridge.fundraising.fundraiser.service.impl;

import com.example.help_bridge.fundraising.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException;
import com.example.help_bridge.fundraising.fundraiser.security.FundraiserAccessPolicy;
import com.example.help_bridge.fundraising.fundraiser.spi.VolunteerDirectory;
import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(FundraiserAssignmentServiceImpl.class)
class AssignmentHistoryPersistenceTest {
    @Autowired TestEntityManager em;
    @Autowired FundraiserAssignmentServiceImpl service;
    @MockitoBean FundraiserAccessPolicy access;
    @MockitoBean VolunteerAccessPolicy volunteerAccess;
    @MockitoBean VolunteerDirectory volunteers;

    FundraiserAssignment activeAssignment() {
        var fundraiser = new Fundraiser();
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        em.persist(fundraiser);
        var assignment = new FundraiserAssignment();
        assignment.setFundraiser(fundraiser);
        assignment.setVolunteerId(5L);
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setAssignedAt(LocalDateTime.of(2026, 11, 1, 10, 0));
        return em.persistAndFlush(assignment);
    }

    @Test void completionCommentSurvivesReloadAndIsReturnedInBothLists() {
        var assignment = activeAssignment();
        Long id = assignment.getId();
        Long fundraiserId = assignment.getFundraiserId();
        String comment = "Допомогу доставлено. " + "я".repeat(479);
        var result = service.completeAssignment(id, new CompleteAssignmentRequest(comment));
        assertEquals(comment, result.closingComment());
        em.flush(); em.clear();

        var stored = em.find(FundraiserAssignment.class, id);
        assertEquals(comment, stored.getClosingComment());
        assertEquals(AssignmentStatus.COMPLETED, stored.getStatus());
        assertTrue(java.time.Duration.between(result.finishedAt(), stored.getFinishedAt()).abs().toNanos() < 1000);
        assertEquals(FundraiserStatus.IN_PROGRESS, stored.getFundraiser().getStatus());
        var personal = service.getVolunteerAssignments(5L).getFirst();
        var team = service.getFundraiserAssignments(fundraiserId).getFirst();
        assertEquals(personal, team);
        assertEquals(fundraiserId, personal.fundraiserId());
        assertEquals("Збір №" + fundraiserId, personal.fundraiserTitle());
        assertEquals(FundraiserStatus.IN_PROGRESS, personal.fundraiserStatus());
        assertEquals(comment, personal.closingComment());
        assertEquals(stored.getAssignedAt(), personal.assignedAt());
        assertNull(personal.returnReason());
    }

    @Test void returnedReasonOf500CharactersSurvivesReload() {
        var assignment = activeAssignment();
        String reason = "р".repeat(500);
        service.returnAssignment(assignment.getId(), new ReturnAssignmentRequest(reason));
        em.flush(); em.clear();
        var history = service.getVolunteerAssignments(5L).getFirst();
        assertEquals(AssignmentStatus.RETURNED, history.status());
        assertEquals(reason, history.returnReason());
        assertNotNull(history.finishedAt());
        assertNull(history.closingComment());
    }

    @Test void oldCompletedAssignmentsWithoutCommentsRemainReadable() {
        var assignment = activeAssignment();
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setFinishedAt(LocalDateTime.now());
        em.flush(); em.clear();
        assertNull(service.getVolunteerAssignments(5L).getFirst().closingComment());
    }

    @Test void repeatedCompletionCannotOverwriteStoredComment() {
        var assignment = activeAssignment();
        service.completeAssignment(assignment.getId(), new CompleteAssignmentRequest("Допомогу доставлено"));
        assertThrows(InvalidAssignmentStateException.class, () -> service.completeAssignment(assignment.getId(),
                new CompleteAssignmentRequest("Інший коментар завершення")));
        em.flush(); em.clear();
        assertEquals("Допомогу доставлено", service.getVolunteerAssignments(5L).getFirst().closingComment());
    }
}
