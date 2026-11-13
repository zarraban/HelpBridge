package com.example.help_bridge.fundraising.fundraiser.service;

import com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserAssignmentResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.VolunteerFundraiserResponse;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VolunteerWorkspaceService {
    private final VolunteerAccessPolicy access;
    private final FundraiserAssignmentService assignments;
    private final FundraiserService fundraisers;
    private final RequestRepository requests;

    public List<FundraiserAssignmentResponse> getMyAssignments() {
        return assignments.getVolunteerAssignments(access.currentVolunteerId()).stream()
                .sorted(Comparator.comparing(FundraiserAssignmentResponse::assignedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FundraiserAssignmentResponse::assignmentId, Comparator.reverseOrder()))
                .toList();
    }

    public FundraiserAssignmentResponse getMyAssignment(Long id) {
        return getMyAssignments().stream().filter(a -> id.equals(a.assignmentId())).findFirst()
                .orElseThrow(() -> new AccessDeniedException("This assignment is not yours or is unavailable"));
    }

    public VolunteerFundraiserResponse getMyFundraiser(Long id) {
        var ownAssignments = getMyAssignments().stream().filter(a -> id.equals(a.fundraiserId())).toList();
        if (ownAssignments.isEmpty()) throw new AccessDeniedException("You are not assigned to this fundraiser");
        var fundraiser = fundraisers.getFundraiserById(id);
        VolunteerFundraiserResponse.RequestSummary summary = fundraiser.requestId() == null ? null
                : requests.findById(fundraiser.requestId()).map(request -> new VolunteerFundraiserResponse.RequestSummary(
                        request.getAssistanceType().getName(), request.getAmount(), request.getDeadline(),
                        request.getInstitutionName(), request.getNeedDescription())).orElse(null);
        return new VolunteerFundraiserResponse(fundraiser, summary, ownAssignments);
    }
}
