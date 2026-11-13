package com.example.help_bridge.fundraising.fundraiser.security;

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

    private static AccessDeniedException denied() {
        return new AccessDeniedException("You do not have access to this fundraiser");
    }
}
