package com.example.help_bridge.fundraising.request;

import com.example.help_bridge.fundraising.request.entity.*;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.*;
import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.CreateDocumentRequest;
import com.example.help_bridge.fundraising.request.exception.*;
import com.example.help_bridge.fundraising.request.repository.*;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.fundraising.request.service.*;
import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.jpa.show-sql=false", "logging.level.root=WARN"})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@Transactional
class RequestAccessIntegrationTest {
    @Autowired UserRepository users;
    @Autowired RequestRepository requests;
    @Autowired AssistanceTypeRepository types;
    @Autowired FundRepository funds;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired RequestService service;
    @Autowired RequestDocumentService documents;
    @Autowired RequestAccessPolicy access;
    @Autowired MockMvc mvc;
    private User owner;
    private User other;
    private Request own;
    private Request foreign;

    @BeforeEach
    void seed() {
        owner = users.save(new User("Owner", "Test", "owner@access.test", "hash"));
        other = users.save(new User("Other", "Test", "other@access.test", "hash"));
        own = requests.save(request(owner));
        foreign = requests.save(request(other));
        login("APPLICANT", owner.getEmail());
    }

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    private void login(String role, String email) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                role + ":" + email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private Request request(User user) {
        return new Request(user, types.findById("MEDICAL").orElseThrow(), BigDecimal.TEN,
                LocalDate.now().plusDays(10), "Situation", "Need", "Access Clinic", "DOC", true);
    }

    private CreateRequestRequest create(Long id) {
        return new CreateRequestRequest(id, "MEDICAL", BigDecimal.TEN, LocalDate.now().plusDays(10),
                "Situation", "Need", "Access Clinic", "DOC", true);
    }

    private UpdateRequestRequest update() {
        return new UpdateRequestRequest("MEDICAL", BigDecimal.ONE, LocalDate.now().plusDays(20),
                "Updated", "Need", "Access Clinic", "DOC");
    }

    private CreateDocumentRequest document() { return new CreateDocumentRequest("Proof", "https://files.test/proof.pdf"); }

    @Test
    void applicantCannotReadOrChangeForeignRequestsOrSpoofAuthor() {
        assertThat(service.getRequestById(own.getId()).requesterId()).isEqualTo(owner.getId());
        assertThatThrownBy(() -> service.getRequestById(foreign.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.createRequest(create(other.getId()))).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateRequest(foreign.getId(), update())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.deleteRequest(foreign.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.getRequestsByUser(other.getId())).isInstanceOf(AccessDeniedException.class);
        assertThat(service.getAllRequests(null)).extracting(RequestResponse::id).containsExactly(own.getId());
        assertThat(service.searchByInstitution("Access Clinic")).extracting(RequestResponse::id).containsExactly(own.getId());
        assertThat(service.createRequest(create(owner.getId())).requesterId()).isEqualTo(owner.getId());
    }

    @Test
    void documentsRespectOwnershipParentAndPendingStatus() {
        var proof = documents.addDocument(own.getId(), document());
        var secondOwn = requests.save(request(owner));
        assertThatThrownBy(() -> documents.getDocument(secondOwn.getId(), proof.id())).isInstanceOf(RequestDocumentNotFoundException.class);
        assertThatThrownBy(() -> documents.getDocuments(foreign.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> documents.addDocument(foreign.getId(), document())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> documents.updateDocument(foreign.getId(), proof.id(), document())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> documents.deleteDocument(foreign.getId(), proof.id())).isInstanceOf(AccessDeniedException.class);
        assertThat(documents.updateDocument(own.getId(), proof.id(), document()).id()).isEqualTo(proof.id());
        own.approve();
        assertThat(documents.getDocuments(own.getId())).hasSize(1);
        assertThatThrownBy(() -> documents.addDocument(own.getId(), document())).isInstanceOf(InvalidRequestStateException.class);
        assertThatThrownBy(() -> documents.updateDocument(own.getId(), proof.id(), document())).isInstanceOf(InvalidRequestStateException.class);
        assertThatThrownBy(() -> documents.deleteDocument(own.getId(), proof.id())).isInstanceOf(InvalidRequestStateException.class);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = RequestStatus.class, names = "PENDING_VERIFICATION", mode = org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE)
    void allReviewedStatusesLockDocumentsForOwnerAndAdmin(RequestStatus status) {
        var proof = documents.addDocument(own.getId(), document());
        org.springframework.test.util.ReflectionTestUtils.setField(own, "status", status);
        requests.saveAndFlush(own);
        for (String role : List.of("APPLICANT", "SYSTEM_ADMIN")) {
            login(role, owner.getEmail());
            assertThat(access.currentViewer().permissions(service.getRequestById(own.getId())).manageDocuments()).isFalse();
            assertThat(documents.getDocuments(own.getId())).hasSize(1);
            assertThatThrownBy(() -> documents.addDocument(own.getId(), document())).isInstanceOf(InvalidRequestStateException.class);
            assertThatThrownBy(() -> documents.updateDocument(own.getId(), proof.id(), document())).isInstanceOf(InvalidRequestStateException.class);
            assertThatThrownBy(() -> documents.deleteDocument(own.getId(), proof.id())).isInstanceOf(InvalidRequestStateException.class);
        }
    }

    @Test
    void adminCanManageOtherOwnersButCannotBypassStatuses() {
        login("SYSTEM_ADMIN", "admin@test.com");
        service.updateRequest(foreign.getId(), update());
        var proof = documents.addDocument(foreign.getId(), document());
        foreign.approve();
        assertThatThrownBy(() -> service.updateRequest(foreign.getId(), update())).isInstanceOf(InvalidRequestStateException.class);
        assertThatThrownBy(() -> documents.deleteDocument(foreign.getId(), proof.id())).isInstanceOf(InvalidRequestStateException.class);
        service.deleteRequest(own.getId());
        assertThat(requests.existsById(own.getId())).isFalse();
    }

    @Test
    void representativeOnlySeesPoolAndOwnFundRequests() {
        Fund fund = funds.save(new Fund("Access Fund", "98765432", "UA00", "Kyiv", "Kyiv",
                "+380501112233", "fund@access.test", "https://fund.test", Map.of()));
        representatives.save(new FundRepresentative("Rep", "Test", "rep@access.test", "+380501112233", "hash", fund));
        var pool = requests.save(request(other));
        pool.approve();
        own.approve();
        own.book(fund);
        var proof = new RequestDocument("Proof", "https://files.test/proof.pdf");
        own.addDocument(proof);
        requests.saveAndFlush(own);
        login("FUND_REPRESENTATIVE", "rep@access.test");
        assertThat(service.getRequestById(own.getId()).id()).isEqualTo(own.getId());
        assertThat(service.getRequestById(pool.getId()).id()).isEqualTo(pool.getId());
        assertThat(documents.getDocuments(own.getId())).hasSize(1);
        assertThatThrownBy(() -> service.getRequestById(foreign.getId())).isInstanceOf(AccessDeniedException.class);
        Fund otherFund = funds.save(new Fund("Other Fund", "98765431", "UA00", "Kyiv", "Kyiv",
                "+380501112234", "otherfund@access.test", "https://otherfund.test", Map.of()));
        foreign.approve();
        foreign.book(otherFund);
        assertThatThrownBy(() -> service.getRequestById(foreign.getId())).isInstanceOf(AccessDeniedException.class);
        assertThat(service.searchByInstitution("Access Clinic")).extracting(RequestResponse::id)
                .containsExactlyInAnyOrder(own.getId(), pool.getId());
        assertThatThrownBy(() -> service.updateRequest(own.getId(), update())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> documents.addDocument(own.getId(), document())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void unauthenticatedAndVolunteerServiceCallsAreDenied() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> service.getAllRequests(null)).isInstanceOf(AccessDeniedException.class);
        login("VOLUNTEER", "volunteer@test.com");
        assertThatThrownBy(() -> service.getRequestById(own.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> documents.getDocuments(own.getId())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void permissionsMatchStatusAndApiEnforcesOwnership() throws Exception {
        var viewer = access.currentViewer();
        assertThat(viewer.permissions(own).edit()).isTrue();
        assertThat(viewer.permissions(foreign).view()).isFalse();
        own.approve();
        assertThat(viewer.permissions(own).edit()).isFalse();
        assertThat(viewer.permissions(own).delete()).isTrue();
        own.transitionTo(RequestStatus.IN_PROGRESS);
        assertThat(viewer.permissions(own).delete()).isFalse();
        mvc.perform(get("/api/v1/requests/{id}", foreign.getId())
                        .with(user("APPLICANT:" + owner.getEmail()).roles("APPLICANT")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/requests/{id}/documents", foreign.getId())
                        .with(user("APPLICANT:" + owner.getEmail()).roles("APPLICANT")))
                .andExpect(status().isForbidden());
    }
}
