package com.example.help_bridge.fundraising.fundraiser;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.repository.*;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.repository.*;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.jpa.show-sql=false", "logging.level.root=WARN"})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@Transactional
class FundraiserAccessIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired jakarta.persistence.EntityManager em;
    @Autowired org.springframework.jdbc.core.simple.JdbcClient jdbc;
    @Autowired FundRepository funds;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired UserRepository users;
    @Autowired RequestRepository requests;
    @Autowired AssistanceTypeRepository types;
    @Autowired FundraiserRepository fundraisers;
    @Autowired EvidenceRepository evidences;
    Fundraiser own;
    Fundraiser foreign;
    Evidence foreignEvidence;

    @BeforeEach
    void seed() {
        var ownFund = funds.save(new Fund("Own", "87654321", "Bank", "Address", "Address", "123", "own@fund.test", "https://fund.test", Map.of()));
        var otherFund = funds.save(new Fund("Other", "87654322", "Bank", "Address", "Address", "123", "other@fund.test", "https://fund.test", Map.of()));
        representatives.save(new FundRepresentative("Rep", "Test", "rep@access.test", "123", "hash", ownFund));
        var owner = users.save(new User("Owner", "Test", "owner@fundraiser.test", "hash"));
        own = createFundraiser(owner, ownFund);
        foreign = createFundraiser(owner, otherFund);
        foreignEvidence = new Evidence();
        foreignEvidence.setFundraiser(foreign);
        foreignEvidence.setReceiptNumber("R-1");
        foreignEvidence.setCreatedAt(java.time.LocalDateTime.now());
        foreignEvidence = evidences.save(foreignEvidence);
    }

    private Fundraiser createFundraiser(User owner, Fund fund) {
        var request = new Request(owner, types.findById("MEDICAL").orElseThrow(), BigDecimal.TEN,
                LocalDate.now().plusDays(10), "Situation", "Need", "Clinic", "DOC", false);
        request.approve();
        request.book(fund);
        request = requests.save(request);
        var fundraiser = new Fundraiser();
        fundraiser.setRequestId(request.getId());
        fundraiser.setStatus(FundraiserStatus.IN_PROGRESS);
        return fundraisers.saveAndFlush(fundraiser);
    }

    @Test
    void representativeOnlyListsAndReadsOwnFundraisers() throws Exception {
        var rep = user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE");
        mvc.perform(get("/api/fundraiser").with(rep)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fundraiserId").value(own.getId()));
        mvc.perform(get("/api/fundraiser/{id}", own.getId()).with(rep)).andExpect(status().isOk());
        mvc.perform(get("/api/fundraiser/{id}", foreign.getId()).with(rep)).andExpect(status().isForbidden());
        mvc.perform(get("/api/fundraisers/{id}/evidences", foreign.getId()).with(rep)).andExpect(status().isForbidden());
        mvc.perform(post("/api/fundraiser/{id}/close", foreign.getId()).with(rep)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/evidences/{id}", foreignEvidence.getId()).with(rep)).andExpect(status().isForbidden());
    }

    @Test
    void adminReadsAllFundsAndTheirEvidence() throws Exception {
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        mvc.perform(get("/api/fundraiser").with(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/fundraiser/{id}", foreign.getId()).with(admin)).andExpect(status().isOk());
        mvc.perform(get("/api/fundraisers/{id}/evidences", foreign.getId()).with(admin)).andExpect(status().isOk());
    }

    @Test
    void unrelatedRolesCannotReadFundraiserOrEvidence() throws Exception {
        for (String role : new String[]{"APPLICANT", "VOLUNTEER", "DONOR"}) {
            var viewer = user(role + ":owner@fundraiser.test").roles(role);
            mvc.perform(get("/api/fundraiser").with(viewer)).andExpect(status().isForbidden());
            mvc.perform(get("/api/fundraiser/{id}", own.getId()).with(viewer)).andExpect(status().isForbidden());
            mvc.perform(get("/api/fundraisers/{id}/evidences", own.getId()).with(viewer)).andExpect(status().isForbidden());
        }
    }
    @Test
    void paginationCountsOnlyOwnFundAndUsesStableIdOrderForEqualDates() throws Exception {
        var extra = new Fundraiser();
        extra.setRequestId(own.getRequestId());
        extra.setStatus(FundraiserStatus.IN_PROGRESS);
        extra = fundraisers.saveAndFlush(extra);
        jdbc.sql("update fundraiser set created_at = '2026-01-01 12:00:00'").update();
        var rep = user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE");
        mvc.perform(get("/api/fundraiser").with(rep).param("size", "1"))
                .andExpect(status().isOk()).andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string("X-Total-Pages", "2"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fundraiserId").value(own.getId()));
        mvc.perform(get("/api/fundraiser").with(rep).param("size", "1").param("page", "2"))
                .andExpect(status().isOk()).andExpect(header().string("X-Page", "2"))
                .andExpect(jsonPath("$[0].fundraiserId").value(extra.getId()));
        mvc.perform(get("/api/fundraiser").with(rep).param("size", "1").param("sort", "desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].fundraiserId").value(extra.getId()));
        mvc.perform(get("/api/fundraiser").with(rep).param("page", "3").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0))
                .andExpect(header().string("X-Total-Count", "2"));
    }

    @Test
    void adminCanSortByDatesAndStatus() throws Exception {
        jdbc.sql("update fundraiser set created_at = '2026-01-01 00:00:00', closed_at = '2026-01-03 00:00:00', status = 'CLOSED' where id = ?")
                .param(own.getId()).update();
        jdbc.sql("update fundraiser set created_at = '2026-01-02 00:00:00', closed_at = '2026-01-04 00:00:00' where id = ?")
                .param(foreign.getId()).update();
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        for (String field : new String[]{"createdAt", "closedAt"}) {
            mvc.perform(get("/api/fundraiser").with(admin).param("sortBy", field).param("sort", "desc").param("size", "1"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$[0].fundraiserId").value(foreign.getId()))
                    .andExpect(header().string("X-Total-Count", "2"));
        }
        mvc.perform(get("/api/fundraiser").with(admin).param("sortBy", "status").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].fundraiserId").value(own.getId()));
    }

    @Test
    void invalidPaginationAndSortingReturnBadRequest() throws Exception {
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        for (String[] parameter : new String[][]{
                {"page", "0"}, {"page", "-1"}, {"page", "2147483648"}, {"page", "9223372036854775807"},
                {"page", "abc"}, {"size", "0"}, {"size", "-1"}, {"size", "101"},
                {"sort", "wrong"}, {"sortBy", "requestId"}, {"sortBy", "id desc"}}) {
            mvc.perform(get("/api/fundraiser").with(admin).param(parameter[0], parameter[1]))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/fundraiser").with(admin).param("page", "2147483647").param("size", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webListRendersOnlyOwnFundAndUkrainianStatuses() throws Exception {
        mvc.perform(get("/fundraisers").accept("text/html")
                        .with(user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(view().name("fundraisers/list"))
                .andExpect(model().attribute("fundraisers", hasProperty("numberOfElements", is(1))))
                .andExpect(content().string(containsString("href=\"/fundraisers/" + own.getId() + "\">Збір №" + own.getId())))
                .andExpect(content().string(containsString("В роботі")))
                .andExpect(content().string(containsString("href=\"/requests/" + own.getRequestId() + "\"")));
    }

    @Test
    void webPaginationPreservesSettingsAndSelectedOptions() throws Exception {
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        mvc.perform(get("/fundraisers").accept("text/html").with(admin)
                        .param("size", "1").param("sortBy", "status").param("sort", "desc"))
                .andExpect(status().isOk()).andExpect(model().attribute("fundraisers", hasProperty("numberOfElements", is(1))))
                .andExpect(content().string(containsString("value=\"status\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"desc\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"1\" selected")))
                .andExpect(content().string(containsString("href=\"/fundraisers?page=2&amp;size=1&amp;sortBy=status&amp;sort=desc\"")))
                .andExpect(content().string(not(containsString("rel=\"prev\""))));
        mvc.perform(get("/fundraisers").accept("text/html").with(admin)
                        .param("size", "1").param("sortBy", "status").param("sort", "desc").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/fundraisers?page=1&amp;size=1&amp;sortBy=status&amp;sort=desc\"")))
                .andExpect(content().string(not(containsString("rel=\"next\""))));
        mvc.perform(get("/fundraisers").accept("text/html").with(admin)
                        .param("size", "1").param("sortBy", "status").param("sort", "desc").param("page", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/fundraisers?sortBy=status&sort=desc&size=1&page=2"));
    }

    @Test
    void webEmptyListAndInvalidParametersHaveHtmlPages() throws Exception {
        fundraisers.delete(own);
        fundraisers.flush();
        var rep = user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE");
        mvc.perform(get("/fundraisers").accept("text/html").with(rep))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Зборів поки немає")))
                .andExpect(content().string(not(containsString("<table>"))));
        for (String[] parameter : new String[][]{{"page", "0"}, {"size", "101"}, {"sortBy", "bad"}, {"page", "abc"}}) {
            mvc.perform(get("/fundraisers").accept("text/html").with(rep).param(parameter[0], parameter[1]))
                    .andExpect(status().isBadRequest()).andExpect(view().name("error/4xx"));
        }
    }

    @Test
    void webListRejectsOtherRolesAndRedirectsAnonymousToLogin() throws Exception {
        for (String role : new String[]{"APPLICANT", "VOLUNTEER", "DONOR"}) {
            mvc.perform(get("/fundraisers").accept("text/html")
                            .with(user(role + ":owner@fundraiser.test").roles(role)))
                    .andExpect(status().isForbidden()).andExpect(view().name("error/403"));
        }
        mvc.perform(get("/fundraisers").accept("text/html"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void representativeCanReadOwnDetailsAndEmptyEvidenceState() throws Exception {
        mvc.perform(get("/fundraisers/{id}", own.getId()).accept("text/html")
                        .with(user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andExpect(view().name("fundraisers/details"))
                .andExpect(content().string(containsString("Збір №" + own.getId())))
                .andExpect(content().string(containsString("До цього збору ще не додано доказів.")))
                .andExpect(content().string(containsString("Збір ще не закрито")))
                .andExpect(content().string(containsString("href=\"/requests/" + own.getRequestId() + "\"")))
                .andExpect(content().string(containsString("Clinic")))
                .andExpect(content().string(containsString("href=\"/fundraisers\"")));
    }

    @Test
    void adminDetailsRenderEvidenceAndEscapeTextAndUnsafeAttachment() throws Exception {
        foreignEvidence.setRecipientFeedback("<script>alert('feedback')</script>");
        foreignEvidence.setAttachmentUrl("https://files.test/proof.pdf");
        evidences.save(foreignEvidence);
        em.flush();
        em.clear();
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        mvc.perform(get("/fundraisers/{id}", foreign.getId()).accept("text/html").with(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("R-1")))
                .andExpect(content().string(containsString("href=\"https://files.test/proof.pdf\"")))
                .andExpect(content().string(containsString("rel=\"noopener noreferrer\"")))
                .andExpect(content().string(not(containsString("<script>alert('feedback')</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
        var evidence = evidences.findById(foreignEvidence.getId()).orElseThrow();
        evidence.setAttachmentUrl("javascript:alert(1)");
        evidences.save(evidence);
        em.flush();
        em.clear();
        mvc.perform(get("/fundraisers/{id}", foreign.getId()).accept("text/html").with(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("javascript:"))))
                .andExpect(content().string(containsString("Вкладення відсутнє або посилання недоступне.")));
    }

    @Test
    void webDetailsRejectForeignFundsAndOtherRolesAndHandleMissingIds() throws Exception {
        mvc.perform(get("/fundraisers/{id}", foreign.getId()).accept("text/html")
                        .with(user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isForbidden()).andExpect(view().name("error/403"));
        for (String role : new String[]{"APPLICANT", "VOLUNTEER", "DONOR"}) {
            mvc.perform(get("/fundraisers/{id}", own.getId()).accept("text/html")
                            .with(user(role + ":owner@fundraiser.test").roles(role)))
                    .andExpect(status().isForbidden());
        }
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        mvc.perform(get("/fundraisers/9223372036854775807").accept("text/html").with(admin))
                .andExpect(status().isNotFound()).andExpect(view().name("error/404"));
        mvc.perform(get("/fundraisers/abc").accept("text/html").with(admin))
                .andExpect(status().isBadRequest()).andExpect(view().name("error/4xx"));
    }

    @Test
    void closedFundraiserWithoutRequestStillHasReadableDetails() throws Exception {
        var orphan = new Fundraiser();
        orphan.setRequestId(Long.MAX_VALUE);
        orphan.setStatus(FundraiserStatus.CLOSED);
        orphan.setClosedAt(java.time.LocalDateTime.of(2026, 11, 10, 12, 30));
        orphan = fundraisers.saveAndFlush(orphan);
        mvc.perform(get("/fundraisers/{id}", orphan.getId()).accept("text/html")
                        .with(user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Закритий")))
                .andExpect(content().string(containsString("10.11.2026 12:30")))
                .andExpect(content().string(containsString("Пов’язану заявку не знайдено або її не вказано.")));
    }

    @Test
    void confirmationExplainsMissingEvidenceAndDoesNotOfferSubmit() throws Exception {
        var rep = user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE");
        mvc.perform(get("/fundraisers/{id}/close", own.getId()).accept("text/html").with(rep))
                .andExpect(status().isOk()).andExpect(view().name("fundraisers/close-confirmation"))
                .andExpect(content().string(containsString("Доказів надання допомоги немає")))
                .andExpect(content().string(not(containsString("Так, закрити збір"))));
        mvc.perform(post("/fundraisers/{id}/close", own.getId()).accept("text/html").with(rep)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(redirectedUrl("/fundraisers/" + own.getId()))
                .andExpect(flash().attribute("error", containsString("він не має доказів")));
        org.junit.jupiter.api.Assertions.assertEquals(FundraiserStatus.IN_PROGRESS,
                fundraisers.findById(own.getId()).orElseThrow().getStatus());
    }

    @Test
    void closingWithEvidenceRequiresCsrfAndPreservesDateOnRepeat() throws Exception {
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        em.flush();
        em.clear();
        mvc.perform(get("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(admin))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Так, закрити збір")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("/fundraisers/" + foreign.getId() + "#evidences")));
        org.junit.jupiter.api.Assertions.assertEquals(FundraiserStatus.IN_PROGRESS,
                fundraisers.findById(foreign.getId()).orElseThrow().getStatus());
        mvc.perform(post("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(admin))
                .andExpect(redirectedUrl("/login?expired"));
        org.junit.jupiter.api.Assertions.assertEquals(FundraiserStatus.IN_PROGRESS,
                fundraisers.findById(foreign.getId()).orElseThrow().getStatus());
        mvc.perform(post("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(admin)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(redirectedUrl("/fundraisers/" + foreign.getId()))
                .andExpect(flash().attribute("message", containsString("успішно закрито")));
        em.flush();
        em.clear();
        var closed = fundraisers.findById(foreign.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(FundraiserStatus.CLOSED, closed.getStatus());
        var closedAt = closed.getClosedAt();
        org.junit.jupiter.api.Assertions.assertNotNull(closedAt);
        mvc.perform(post("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(admin)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(redirectedUrl("/fundraisers/" + foreign.getId()))
                .andExpect(flash().attribute("error", containsString("уже закрито")));
        org.junit.jupiter.api.Assertions.assertEquals(closedAt, fundraisers.findById(foreign.getId()).orElseThrow().getClosedAt());
        mvc.perform(get("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(admin))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("Так, закрити збір"))));
    }

    @Test
    void removedEvidenceAfterConfirmationPreventsClosing() throws Exception {
        var admin = user("SYSTEM_ADMIN:admin@test").roles("SYSTEM_ADMIN");
        em.flush();
        em.clear();
        mvc.perform(get("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(admin))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Так, закрити збір")));
        jdbc.sql("delete from evidences where id = ?").param(foreignEvidence.getId()).update();
        em.clear();
        mvc.perform(post("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(admin)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(redirectedUrl("/fundraisers/" + foreign.getId()))
                .andExpect(flash().attribute("error", containsString("він не має доказів")));
        org.junit.jupiter.api.Assertions.assertEquals(FundraiserStatus.IN_PROGRESS,
                fundraisers.findById(foreign.getId()).orElseThrow().getStatus());
    }

    @Test
    void webClosingRejectsForeignFundsAndOtherRolesAndHandlesMissingFundraiser() throws Exception {
        var rep = user("FUND_REPRESENTATIVE:rep@access.test").roles("FUND_REPRESENTATIVE");
        mvc.perform(get("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(rep))
                .andExpect(status().isForbidden());
        mvc.perform(post("/fundraisers/{id}/close", foreign.getId()).accept("text/html").with(rep)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
        for (String role : new String[]{"APPLICANT", "VOLUNTEER", "DONOR"}) {
            var viewer = user(role + ":owner@fundraiser.test").roles(role);
            mvc.perform(get("/fundraisers/{id}/close", own.getId()).accept("text/html").with(viewer))
                    .andExpect(status().isForbidden());
            mvc.perform(post("/fundraisers/{id}/close", own.getId()).accept("text/html").with(viewer)
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(post("/fundraisers/9223372036854775807/close").accept("text/html").with(rep)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(redirectedUrl("/fundraisers"))
                .andExpect(flash().attribute("error", containsString("не знайдено")));
    }

}
