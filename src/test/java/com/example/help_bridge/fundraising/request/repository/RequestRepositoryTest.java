package com.example.help_bridge.fundraising.request.repository;

import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestDocument;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.user.entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private EntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user = persistUser();
    }

    private User persistUser() {
        User u = new User();
        em.persist(u);
        return u;
    }

    private Request newRequest(String institution) {
        return newRequest(user, institution);
    }

    private Request newRequest(User owner, String institution) {
        return new Request(owner, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", institution, "A-1", true);
    }

    private long documentCount() {
        return em.createQuery("select count(d) from RequestDocument d", Long.class).getSingleResult();
    }

    // SessionFactory спільний для всього застосунку, тому його не можна закривати через try-with-resources
    @SuppressWarnings("resource")
    private Statistics statistics() {
        return em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void findAllByStatusWithDetails_loadsEverythingInSingleQuery() {
        for (int i = 0; i < 3; i++) {
            Request request = newRequest("inst-" + i);
            request.addDocument(new RequestDocument("a" + i + ".pdf", "http://x/a" + i));
            request.addDocument(new RequestDocument("b" + i + ".pdf", "http://x/b" + i));
            request.transitionTo(RequestStatus.NEW);
            em.persist(request);
        }
        em.flush();
        em.clear();

        Statistics statistics = statistics();
        statistics.clear();

        List<Request> result = requestRepository.findAllByStatusWithDetails(RequestStatus.NEW);
        result.forEach(r -> {
            assertThat(r.getDocuments()).isNotEmpty();
            assertThat(r.getRequester().getId()).isNotNull();
        });

        assertThat(result).hasSize(3);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void findAllWithDetails_loadsEverythingInSingleQuery() {
        for (int i = 0; i < 3; i++) {
            Request request = newRequest("inst-" + i);
            request.addDocument(new RequestDocument("a" + i + ".pdf", "http://x/a" + i));
            em.persist(request);
        }
        em.flush();
        em.clear();

        Statistics statistics = statistics();
        statistics.clear();

        List<Request> result = requestRepository.findAllWithDetails();
        result.forEach(r -> assertThat(r.getDocuments()).isNotEmpty());

        assertThat(result).hasSize(3);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void findByStatus_returnsOnlyMatchingRequests() {
        Request pending = newRequest("inst-1");
        Request approved = newRequest("inst-2");
        approved.transitionTo(RequestStatus.NEW);
        em.persist(pending);
        em.persist(approved);
        em.flush();

        assertThat(requestRepository.findByStatus(RequestStatus.NEW)).containsExactly(approved);
    }

    @Test
    void findByInstitutionNameIgnoreCase_ignoresCase() {
        em.persist(newRequest("Red Cross"));
        em.flush();

        assertThat(requestRepository.findByInstitutionNameIgnoreCase("red cross")).hasSize(1);
    }

    @Test
    void searchByInstitutionWithDetails_matchesPartOfNameIgnoringCase() {
        em.persist(newRequest("Kyiv City Hospital"));
        em.persist(newRequest("Red Cross"));
        em.flush();
        em.clear();

        List<Request> result = requestRepository.searchByInstitutionWithDetails("hospital");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getInstitutionName()).isEqualTo("Kyiv City Hospital");
    }

    @Test
    void findByRequesterIdWithDetails_returnsOnlyRequestsOfThatUser() {
        User other = persistUser();
        em.persist(newRequest(user, "inst-1"));
        em.persist(newRequest(user, "inst-2"));
        em.persist(newRequest(other, "inst-3"));
        em.flush();
        em.clear();

        List<Request> result = requestRepository.findByRequesterIdWithDetails(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(r ->
                assertThat(r.getRequester().getId()).isEqualTo(user.getId()));
    }

    @Test
    void existsByApplicationNumber_and_countByStatus_work() {
        em.persist(newRequest("inst"));
        em.flush();

        assertThat(requestRepository.existsByApplicationNumber("A-1")).isTrue();
        assertThat(requestRepository.existsByApplicationNumber("missing")).isFalse();
        assertThat(requestRepository.countByStatus(RequestStatus.PENDING_VERIFICATION)).isEqualTo(1);
    }

    @Test
    void savingRequest_cascadesToDocuments() {
        Request request = newRequest("inst");
        request.addDocument(new RequestDocument("a.pdf", "http://x/a"));
        request.addDocument(new RequestDocument("b.pdf", "http://x/b"));

        requestRepository.saveAndFlush(request);

        assertThat(documentCount()).isEqualTo(2);
    }

    @Test
    void removingDocumentFromRequest_deletesOrphan() {
        Request request = newRequest("inst");
        request.addDocument(new RequestDocument("a.pdf", "http://x/a"));
        request.addDocument(new RequestDocument("b.pdf", "http://x/b"));
        em.persist(request);
        em.flush();
        em.clear();

        Request loaded = requestRepository.findByIdWithDocuments(request.getId()).orElseThrow();
        loaded.removeDocument(loaded.getDocuments().getFirst());
        em.flush();

        assertThat(documentCount()).isEqualTo(1);
    }

    @Test
    void deletingRequest_deletesItsDocuments() {
        Request request = newRequest("inst");
        request.addDocument(new RequestDocument("a.pdf", "http://x/a"));
        em.persist(request);
        em.flush();
        em.clear();

        requestRepository.deleteById(request.getId());
        em.flush();

        assertThat(documentCount()).isZero();
    }

    @Test
    void findByIdWithDetails_loadsDocumentsInSingleQuery() {
        Request request = newRequest("inst");
        request.addDocument(new RequestDocument("a.pdf", "http://x/a"));
        request.addDocument(new RequestDocument("b.pdf", "http://x/b"));
        em.persist(request);
        em.flush();
        em.clear();

        Statistics statistics = statistics();
        statistics.clear();

        Request loaded = requestRepository.findByIdWithDetails(request.getId()).orElseThrow();

        assertThat(loaded.getDocuments()).hasSize(2);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}