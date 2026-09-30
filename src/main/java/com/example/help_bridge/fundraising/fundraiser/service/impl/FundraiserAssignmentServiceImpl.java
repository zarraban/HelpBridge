package com.example.help_bridge.fundraising.fundraiser.service.impl;

import com.example.help_bridge.fundraising.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.ReturnAssignmentResponse;
import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserAssignmentJpaRepository;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserJpaRepository;
import com.example.help_bridge.fundraising.fundraiser.service.FundraiserAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.help_bridge.fundraising.fundraiser.exception.AssignmentNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException;

@Service
@RequiredArgsConstructor
public class FundraiserAssignmentServiceImpl implements FundraiserAssignmentService {

    private final FundraiserAssignmentJpaRepository assignmentJpaRepository;
    private final FundraiserJpaRepository FundraiserJpaRepository;

    @Override
    public CompleteAssignmentResponse completeAssignment(Long assignmentId, CompleteAssignmentRequest request) {
        FundraiserAssignment assignment = assignmentJpaRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment with the specified ID was not found"));
        
        if (!assignment.getStatus().canTransitionTo(AssignmentStatus.COMPLETED)) {
            throw new InvalidAssignmentStateException("Cannot transition assignment status from " + assignment.getStatus() + " to COMPLETED");
        }

        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setFinishedAt(LocalDateTime.now());
        assignmentJpaRepository.save(assignment);
        
        Fundraiser fundraiser = FundraiserJpaRepository.findById(assignment.getFundraiserId())
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
        FundraiserAssignment assignment = assignmentJpaRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment with the specified ID was not found"));

        if (!assignment.getStatus().canTransitionTo(AssignmentStatus.RETURNED)) {
            throw new InvalidAssignmentStateException("Cannot transition assignment status from " + assignment.getStatus() + " to RETURNED");
        }

        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnReason(request.returnReason());
        assignment.setFinishedAt(LocalDateTime.now());
        assignmentJpaRepository.save(assignment);
        
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
        
        FundraiserAssignment saved = assignmentJpaRepository.save(assignment);
        
        return new AssignVolunteerResponse(
                saved.getId(), 
                saved.getFundraiserId(),
                saved.getStatus(), 
                saved.getAssignedAt()
        );
    }

    @Override
    public List<FundraiserAssignmentResponse> getFundraiserAssignments(Long fundraiserId) {
        return assignmentJpaRepository.findAll().stream()
                .filter(a -> fundraiserId.equals(a.getFundraiserId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FundraiserAssignmentResponse> getVolunteerAssignments(Long volunteerId) {
        return assignmentJpaRepository.findAll().stream()
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