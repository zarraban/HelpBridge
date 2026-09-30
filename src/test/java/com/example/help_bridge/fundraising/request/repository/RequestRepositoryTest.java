package com.example.help_bridge.fundraising.request.repository;

import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestDocument;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

    private Request newRequest(String institution) {
        return new Request(null, "MEDICAL", BigDecimal.TEN,
                LocalDate.now().plusDays(5), "s", "n", institution, "A-1", true);
    }

    private long documentCount() {
        return em.createQuery("select count(d) from RequestDocument d", Long.class).getSingleResult();
    }

    @Test
    void findAllByStatusWithDetails_loadsDocumentsInSingleQuery() {
        for (int i = 0; i < 3; i++) {
            Request request = newRequest("inst-" + i);
            request.addDocument(new RequestDocument("a" + i + ".pdf", "http://x/a" + i));
            request.addDocument(new RequestDocument("b" + i + ".pdf", "http://x/b" + i));
            request.transitionTo(RequestStatus.NEW);
            em.persist(request);
        }
        em.flush();
        em.clear();

        Statistics statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<Request> result = requestRepository.findAllByStatusWithDetails(RequestStatus.NEW);
        result.forEach(r -> r.getDocuments().size());

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

        Request loaded = requestRepository.findByIdWithDetails(request.getId()).orElseThrow();
        loaded.removeDocument(loaded.getDocuments().get(0));
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
}