package com.example.help_bridge.fundraising.request.security;

import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
public class RequestAccessPolicy {
    private final UserRepository users;
    private final FundRepresentativeRepository representatives;

    public RequestAccessPolicy(UserRepository users, FundRepresentativeRepository representatives) {
        this.users = users;
        this.representatives = representatives;
    }

    @Transactional(readOnly = true)
    public Viewer currentViewer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw denied();
        }
        if (hasRole(auth, "SYSTEM_ADMIN")) return new Viewer("SYSTEM_ADMIN", null, null);
        String name = auth.getName();
        String email = name.substring(name.indexOf(':') + 1);
        if (hasRole(auth, "APPLICANT")) {
            Long id = users.findByEmail(email).orElseThrow(RequestAccessPolicy::denied).getId();
            return new Viewer("APPLICANT", id, null);
        }
        if (hasRole(auth, "FUND_REPRESENTATIVE")) {
            var rep = representatives.findByEmailIgnoreCase(email).orElseThrow(RequestAccessPolicy::denied);
            return new Viewer("FUND_REPRESENTATIVE", null, rep.getFund().getId(), rep.getFund().getStatus());
        }
        throw denied();
    }

    private static boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private static AccessDeniedException denied() {
        return new AccessDeniedException("You do not have access to this request");
    }

    public record Permissions(boolean view, boolean edit, boolean delete, boolean manageDocuments, boolean review,
                              boolean book, boolean ownFundInProgress) {}

    public record Viewer(String role, Long requesterId, Long fundId, FundStatus fundStatus) {
        public Viewer(String role, Long requesterId, Long fundId) {
            this(role, requesterId, fundId, null);
        }

        public void requireReview(Request request) {
            if (!isAdmin()) throw denied();
            if (!permissions(request).review()) {
                throw new InvalidRequestStateException(
                        "Request is no longer pending verification");
            }
        }

        public void requireBook(Request request) {
            if (!isRepresentative() || fundId == null || fundStatus != FundStatus.APPROVED) throw denied();
            if (!permissions(request).book()) {
                throw new InvalidRequestStateException(
                        "Request is no longer available for booking");
            }
        }

        public boolean isAdmin() { return "SYSTEM_ADMIN".equals(role); }
        public boolean isRepresentative() { return "FUND_REPRESENTATIVE".equals(role); }

        public boolean ownsUser(Long id) {
            return "APPLICANT".equals(role) && requesterId != null && Objects.equals(requesterId, id);
        }

        public boolean canRead(Request request) {
            return canRead(request.getRequester().getId(), request.getStatus(),
                    request.getFund() == null ? null : request.getFund().getId());
        }

        private boolean canRead(Long ownerId, RequestStatus status, Long bookedFundId) {
            return isAdmin() || ownsUser(ownerId)
                    || (isRepresentative() && (status == RequestStatus.NEW
                    || (fundId != null && Objects.equals(fundId, bookedFundId))));
        }

        public void requireRead(Request request) {
            if (!canRead(request)) throw denied();
        }

        public void requireManage(Request request) {
            requireCreateFor(request.getRequester().getId());
        }

        public void requireManage(RequestResponse request) {
            requireCreateFor(request.requesterId());
        }

        public void requireCreateFor(Long userId) {
            if (!isAdmin() && !ownsUser(userId)) throw denied();
        }

        public void requireSharedPool() {
            if (!isAdmin() && !isRepresentative()) throw denied();
        }

        public Permissions permissions(Request request) {
            return permissions(request.getRequester().getId(), request.getStatus(),
                    request.getFund() == null ? null : request.getFund().getId());
        }

        public Permissions permissions(RequestResponse request) {
            return permissions(request.requesterId(), request.status(), request.fundId());
        }

        private Permissions permissions(Long ownerId, RequestStatus status, Long bookedFundId) {
            boolean manager = isAdmin() || ownsUser(ownerId);
            boolean pending = status == RequestStatus.PENDING_VERIFICATION;
            return new Permissions(canRead(ownerId, status, bookedFundId), manager && pending,
                    manager && (pending || status == RequestStatus.NEW),
                    manager && pending, isAdmin() && pending,
                    isRepresentative() && fundId != null && fundStatus == FundStatus.APPROVED
                            && status == RequestStatus.NEW && bookedFundId == null,
                    isRepresentative() && fundId != null && Objects.equals(fundId, bookedFundId)
                            && status == RequestStatus.IN_PROGRESS);
        }
    }
}
