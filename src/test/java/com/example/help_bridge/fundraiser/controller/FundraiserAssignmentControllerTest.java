package com.example.help_bridge.fundraiser.controller;

import com.example.help_bridge.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.fundraiser.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.ReturnAssignmentResponse;
import com.example.help_bridge.fundraiser.service.FundraiserAssignmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundraiserAssignmentControllerTest {

    @Mock
    private FundraiserAssignmentService assignmentService;

    @InjectMocks
    private FundraiserAssignmentController assignmentController;


    @Test
    void completeAssignment_shouldInvokeService() {
        Long assignmentId = 1L;
        CompleteAssignmentRequest request = mock(CompleteAssignmentRequest.class);
        CompleteAssignmentResponse response = mock(CompleteAssignmentResponse.class);
        when(assignmentService.completeAssignment(assignmentId, request)).thenReturn(response);

        ResponseEntity<CompleteAssignmentResponse> result = assignmentController.completeAssignment(assignmentId, request);

        verify(assignmentService, times(1)).completeAssignment(assignmentId, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void returnAssignment_shouldInvokeService() {
        Long assignmentId = 1L;
        ReturnAssignmentRequest request = mock(ReturnAssignmentRequest.class);
        ReturnAssignmentResponse response = mock(ReturnAssignmentResponse.class);
        when(assignmentService.returnAssignment(assignmentId, request)).thenReturn(response);

        ResponseEntity<ReturnAssignmentResponse> result = assignmentController.returnAssignment(assignmentId, request);

        verify(assignmentService, times(1)).returnAssignment(assignmentId, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void assignVolunteerToFund_shouldInvokeService() {
        Long fundraiserId = 1L;
        AssignVolunteerRequest request = mock(AssignVolunteerRequest.class);
        AssignVolunteerResponse response = mock(AssignVolunteerResponse.class);
        when(assignmentService.assignVolunteerToFund(fundraiserId, request)).thenReturn(response);

        ResponseEntity<AssignVolunteerResponse> result = assignmentController.assignVolunteerToFund(fundraiserId, request);

        verify(assignmentService, times(1)).assignVolunteerToFund(fundraiserId, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void fundraiserAssignments_shouldInvokeService() {
        Long fundraiserId = 1L;
        List<FundraiserAssignmentResponse> responses = List.of(mock(FundraiserAssignmentResponse.class));
        when(assignmentService.getFundraiserAssignments(fundraiserId)).thenReturn(responses);

        ResponseEntity<List<FundraiserAssignmentResponse>> result = assignmentController.fundraiserAssignments(fundraiserId);

        verify(assignmentService, times(1)).getFundraiserAssignments(fundraiserId);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
    }

    @Test
    void volunteersAssignments_shouldInvokeService() {
        Long volunteerId = 1L;
        List<FundraiserAssignmentResponse> responses = List.of(mock(FundraiserAssignmentResponse.class));
        when(assignmentService.getVolunteerAssignments(volunteerId)).thenReturn(responses);

        ResponseEntity<List<FundraiserAssignmentResponse>> result = assignmentController.volunteersAssignments(volunteerId);

        verify(assignmentService, times(1)).getVolunteerAssignments(volunteerId);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
    }
}
