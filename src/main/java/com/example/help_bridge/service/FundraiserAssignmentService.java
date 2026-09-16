package com.example.help_bridge.service;

import com.example.help_bridge.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.dto.response.ReturnAssignmentResponse;
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
