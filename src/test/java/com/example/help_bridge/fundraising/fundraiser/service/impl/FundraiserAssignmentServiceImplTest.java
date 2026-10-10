package com.example.help_bridge.fundraising.fundraiser.service.impl;

import com.example.help_bridge.fundraising.fundraiser.spi.VolunteerDirectory;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraising.fundraiser.exception.AssignmentNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException;
import com.example.help_bridge.fundraising.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserAssignmentRepository;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundraiserAssignmentServiceImplTest {

    @Mock
    private FundraiserAssignmentRepository assignmentJpaRepository;

    @Mock
    private FundraiserRepository fundraiserRepository;

    @Mock
    private VolunteerDirectory volunteerDirectory;

    @InjectMocks
    private FundraiserAssignmentServiceImpl assignmentService;

    @Test
    void completeAssignment_shouldThrowAssignmentNotFoundException_whenAssignmentNotFound() {
        when(assignmentJpaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AssignmentNotFoundException.class,
            () -> assignmentService.completeAssignment(1L, new CompleteAssignmentRequest("comment")));
    }

    @Test
    void completeAssignment_shouldThrowInvalidAssignmentStateException_whenAlreadyCompleted() {
        FundraiserAssignment assignment = new FundraiserAssignment();
        assignment.setStatus(AssignmentStatus.COMPLETED);
        when(assignmentJpaRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(InvalidAssignmentStateException.class,
            () -> assignmentService.completeAssignment(1L, new CompleteAssignmentRequest("comment")));
    }

    @Test
    void completeAssignment_shouldChangeStateAndSave_whenActive() {
        FundraiserAssignment assignment = new FundraiserAssignment();
        assignment.setId(1L);
        assignment.setFundraiserId(100L);
        assignment.setStatus(AssignmentStatus.ACTIVE);

        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(100L);

        when(assignmentJpaRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(fundraiserRepository.findById(100L)).thenReturn(Optional.of(fundraiser));

        assignmentService.completeAssignment(1L, new CompleteAssignmentRequest("comment"));

        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        assertNotNull(assignment.getFinishedAt());
        verify(assignmentJpaRepository).save(assignment);
    }

    @Test
    void assignVolunteerToFund_shouldMoveFundraiserToInProgress() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(100L);
        fundraiser.setStatus(FundraiserStatus.PENDING_ASSIGNMENT);
        when(fundraiserRepository.findById(100L)).thenReturn(Optional.of(fundraiser));
        when(assignmentJpaRepository.save(any(FundraiserAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        assignmentService.assignVolunteerToFund(100L,
                new AssignVolunteerRequest(5L));

        verify(volunteerDirectory).requireActiveVolunteer(5L);
        assertEquals(FundraiserStatus.IN_PROGRESS, fundraiser.getStatus());
    }

    @Test
    void assignVolunteerToFund_shouldRejectClosedFundraiser() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(100L);
        fundraiser.setStatus(FundraiserStatus.CLOSED);
        when(fundraiserRepository.findById(100L)).thenReturn(Optional.of(fundraiser));

        assertThrows(InvalidAssignmentStateException.class, () -> assignmentService.assignVolunteerToFund(100L,
                new AssignVolunteerRequest(5L)));
        verify(assignmentJpaRepository, never()).save(any());
    }

    @Test
    void assignVolunteerToFund_shouldRejectDuplicateActiveAssignment() {
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setId(100L);
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        when(fundraiserRepository.findById(100L)).thenReturn(Optional.of(fundraiser));
        when(assignmentJpaRepository.existsByFundraiserIdAndVolunteerIdAndStatus(100L, 5L, AssignmentStatus.ACTIVE))
                .thenReturn(true);

        assertThrows(InvalidAssignmentStateException.class, () -> assignmentService.assignVolunteerToFund(100L,
                new AssignVolunteerRequest(5L)));
    }
}