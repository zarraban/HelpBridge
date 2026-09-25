package com.example.help_bridge.fundraiser.service.impl;

import com.example.help_bridge.fundraiser.exception.AssignmentNotFoundException;
import com.example.help_bridge.fundraiser.exception.InvalidAssignmentStateException;
import com.example.help_bridge.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraiser.entity.FundraiserAssignment;
import com.example.help_bridge.fundraiser.repository.FundraiserAssignmentRepository;
import com.example.help_bridge.fundraiser.repository.FundraiserRepository;
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
    private FundraiserAssignmentRepository assignmentRepository;

    @Mock
    private FundraiserRepository fundraiserRepository;

    @InjectMocks
    private FundraiserAssignmentServiceImpl assignmentService;

    @Test
    void completeAssignment_shouldThrowAssignmentNotFoundException_whenAssignmentNotFound() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AssignmentNotFoundException.class, 
            () -> assignmentService.completeAssignment(1L, new CompleteAssignmentRequest("comment")));
    }

    @Test
    void completeAssignment_shouldThrowInvalidAssignmentStateException_whenAlreadyCompleted() {
        FundraiserAssignment assignment = new FundraiserAssignment();
        assignment.setStatus(AssignmentStatus.COMPLETED);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

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

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(fundraiserRepository.findById(100L)).thenReturn(Optional.of(fundraiser));

        assignmentService.completeAssignment(1L, new CompleteAssignmentRequest("comment"));

        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        assertNotNull(assignment.getFinishedAt());
        verify(assignmentRepository).save(assignment);
    }
}