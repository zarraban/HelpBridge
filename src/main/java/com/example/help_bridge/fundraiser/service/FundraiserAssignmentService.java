package com.example.help_bridge.fundraiser.service;

import com.example.help_bridge.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.fundraiser.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.ReturnAssignmentResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundraiserAssignmentService {

    public CompleteAssignmentResponse completeAssignment(Long assignmentId, CompleteAssignmentRequest request) {
        return null;
    }

    public ReturnAssignmentResponse returnAssignment(Long assignmentId, ReturnAssignmentRequest request) {
        return null;
    }

    public AssignVolunteerResponse assignVolunteerToFund(Long fundraiserId, AssignVolunteerRequest request) {
        return null;
    }

    public List<FundraiserAssignmentResponse> getFundraiserAssignments(Long fundraiserId) {
        return null;
    }

    public List<FundraiserAssignmentResponse> getVolunteerAssignments(Long volunteerId) {
        return null;
    }
}
