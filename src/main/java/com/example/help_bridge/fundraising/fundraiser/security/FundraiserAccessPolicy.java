package com.example.help_bridge.fundraising.fundraiser.security;

import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserAssignmentRepository;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;

import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FundraiserAccessPolicy {
    private final RequestAccessPolicy requestAccess;
    private final VolunteerAccessPolicy volunteers;
    private final FundraiserAssignmentRepository assignments;
    private final RequestRepository requests;

    public RequestAccessPolicy.Viewer currentViewer() {
        var viewer = requestAccess.currentViewer();
        if (!viewer.isAdmin() && !(viewer.isRepresentative() && viewer.fundId() != null)) {
            throw denied();
        }
        return viewer;
    }

    public void requireAccess(Fundraiser fundraiser) {
        var viewer = currentViewer();
        if (viewer.isAdmin()) return;
        if (fundraiser.getRequestId() == null || !requests.existsByIdAndFundId(fundraiser.getRequestId(), viewer.fundId())) {
            throw denied();
        }
    }

    public void requireView(Fundraiser fundraiser) {
        if (volunteers.isVolunteer()) {
            Long volunteerId = volunteers.currentVolunteerId();
            if (assignments.findByFundraiserId(fundraiser.getId()).stream()
                    .noneMatch(a -> volunteerId.equals(a.getVolunteerId()))) throw denied();
        } else {
            requireAccess(fundraiser);
        }
    }

    public void requireAssignmentAction(FundraiserAssignment assignment) {
        if (volunteers.isVolunteer()) {
            if (!volunteers.currentVolunteerId().equals(assignment.getVolunteerId())) throw denied();
        } else {
            requireRepresentative(assignment.getFundraiser());
        }
    }

    public void requireRepresentative(Fundraiser fundraiser) {
        if (!currentViewer().isRepresentative()) throw denied();
        requireAccess(fundraiser);
    }

    public void requireAddEvidence(Fundraiser fundraiser) {
        if (volunteers.isVolunteer()) {
            if (!assignments.existsByFundraiserIdAndVolunteerIdAndStatus(fundraiser.getId(),
                    volunteers.currentVolunteerId(), AssignmentStatus.ACTIVE)) throw denied();
        } else {
            requireRepresentative(fundraiser);
        }
    }

    public void requireAssignableVolunteer(Fundraiser fundraiser, Long volunteerId) {
        requireAccess(fundraiser);
        var request = requests.findById(fundraiser.getRequestId()).orElseThrow(FundraiserAccessPolicy::denied);
        volunteers.requireActiveInFund(volunteerId, request.getFund() == null ? null : request.getFund().getId());
    }

    private static AccessDeniedException denied() {
        return new AccessDeniedException("You do not have access to this fundraiser");
    }
}
