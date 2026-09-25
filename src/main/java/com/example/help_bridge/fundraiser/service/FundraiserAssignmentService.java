package com.example.help_bridge.fundraiser.service;

import com.example.help_bridge.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraiser.dto.response.AssignVolunteerResponse;
import com.example.help_bridge.fundraiser.dto.response.CompleteAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.fundraiser.dto.response.ReturnAssignmentResponse;
import java.util.List;

public interface FundraiserAssignmentService {
    CompleteAssignmentResponse completeAssignment(Long assignmentId, CompleteAssignmentRequest request);
    ReturnAssignmentResponse returnAssignment(Long assignmentId, ReturnAssignmentRequest request);
    AssignVolunteerResponse assignVolunteerToFund(Long fundraiserId, AssignVolunteerRequest request);
    List<FundraiserAssignmentResponse> getFundraiserAssignments(Long fundraiserId);
    List<FundraiserAssignmentResponse> getVolunteerAssignments(Long volunteerId);
}
