package com.example.help_bridge.users.volunteer.security;

import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;

import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VolunteerAccessPolicy {
    private final RequestAccessPolicy requestAccess;
    private final VolunteerRepository volunteers;

    public void requireManageFund(Long fundId) {
        requestAccess.currentViewer().requireBookingFund(fundId);
    }

    public Long currentVolunteerId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken
                || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_VOLUNTEER"))) {
            throw new AccessDeniedException("Volunteer access required");
        }
        String name = auth.getName();
        return volunteers.findByEmail(name.substring(name.indexOf(':') + 1))
                .filter(v -> v.isActive()).orElseThrow(() -> new AccessDeniedException("Active volunteer required")).getId();
    }

    public boolean isVolunteer() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VOLUNTEER"));
    }

    public void requireReadVolunteer(Long volunteerId) {
        if (isVolunteer()) {
            if (!currentVolunteerId().equals(volunteerId)) throw new AccessDeniedException("Not your assignments");
        } else {
            var volunteer = volunteers.findById(volunteerId)
                    .orElseThrow(() -> new AccessDeniedException("Volunteer access denied"));
            requireManageFund(volunteer.getFund().getId());
        }
    }

    public void requireActiveInFund(Long volunteerId, Long fundId) {
        if (fundId == null || volunteers.findByFundIdAndIdAndStatus(fundId, volunteerId,
                VolunteerStatus.ACTIVE).isEmpty()) {
            throw new AccessDeniedException("Volunteer must be active in the fundraiser's fund");
        }
    }
}
