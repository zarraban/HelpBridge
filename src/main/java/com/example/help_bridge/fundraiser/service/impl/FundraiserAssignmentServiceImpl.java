package com.example.help_bridge.fundraiser.service.impl;

import com.example.help_bridge.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.fundraiser.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.ReturnAssignmentResponse;
import com.example.help_bridge.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraiser.entity.FundraiserAssignment;
import com.example.help_bridge.fundraiser.repository.FundraiserAssignmentRepository;
import com.example.help_bridge.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraiser.service.FundraiserAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.help_bridge.fundraiser.exception.AssignmentNotFoundException;
import com.example.help_bridge.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraiser.exception.InvalidAssignmentStateException;

@Service
@RequiredArgsConstructor
public class FundraiserAssignmentServiceImpl implements FundraiserAssignmentService {

    private final FundraiserAssignmentRepository assignmentRepository;
    private final FundraiserRepository fundraiserRepository;

    @Override
    public CompleteAssignmentResponse completeAssignment(Long assignmentId, CompleteAssignmentRequest request) {
        FundraiserAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment with the specified ID was not found"));
        
        if (!assignment.getStatus().canTransitionTo(AssignmentStatus.COMPLETED)) {
            throw new InvalidAssignmentStateException("Cannot transition assignment status from " + assignment.getStatus() + " to COMPLETED");
        }

        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setFinishedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
        
        Fundraiser fundraiser = fundraiserRepository.findById(assignment.getFundraiserId())
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser with the specified ID was not found"));
                
        int evidenceCount = fundraiser.getEvidences() == null ? 0 : fundraiser.getEvidences().size();

        return new CompleteAssignmentResponse(
                assignment.getId(), 
                assignment.getFundraiserId(),
                assignment.getStatus(), 
                assignment.getFinishedAt(),
                fundraiser.getStatus(),
                evidenceCount
        );
    }

    @Override
    public ReturnAssignmentResponse returnAssignment(Long assignmentId, ReturnAssignmentRequest request) {
        FundraiserAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment with the specified ID was not found"));

        if (!assignment.getStatus().canTransitionTo(AssignmentStatus.RETURNED)) {
            throw new InvalidAssignmentStateException("Cannot transition assignment status from " + assignment.getStatus() + " to RETURNED");
        }

        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnReason(request.returnReason());
        assignment.setFinishedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
        
        return new ReturnAssignmentResponse(
                assignment.getId(), 
                assignment.getFundraiserId(),
                assignment.getStatus(), 
                assignment.getFinishedAt(),
                assignment.getReturnReason()
        );
    }

    @Override
    public AssignVolunteerResponse assignVolunteerToFund(Long fundraiserId, AssignVolunteerRequest request) {
        FundraiserAssignment assignment = new FundraiserAssignment();
        assignment.setFundraiserId(fundraiserId);
        assignment.setVolunteerId(request.volunteerId());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setAssignedAt(LocalDateTime.now());
        
        FundraiserAssignment saved = assignmentRepository.save(assignment);
        
        return new AssignVolunteerResponse(
                saved.getId(), 
                saved.getFundraiserId(),
                saved.getStatus(), 
                saved.getAssignedAt()
        );
    }

    @Override
    public List<FundraiserAssignmentResponse> getFundraiserAssignments(Long fundraiserId) {
        return assignmentRepository.findAll().stream()
                .filter(a -> fundraiserId.equals(a.getFundraiserId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FundraiserAssignmentResponse> getVolunteerAssignments(Long volunteerId) {
        return assignmentRepository.findAll().stream()
                .filter(a -> volunteerId.equals(a.getVolunteerId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private FundraiserAssignmentResponse mapToResponse(FundraiserAssignment a) {
        return new FundraiserAssignmentResponse(
                a.getId(),
                a.getVolunteerId(),
                a.getStatus(),
                a.getAssignedAt(),
                a.getFinishedAt(),
                a.getReturnReason()
        );
    }
}