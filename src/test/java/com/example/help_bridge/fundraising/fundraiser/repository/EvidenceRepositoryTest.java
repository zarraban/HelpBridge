package com.example.help_bridge.fundraising.fundraiser.repository;

import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class EvidenceRepositoryTest {

    @Autowired
    private EvidenceRepository repository;

    @Autowired
    private TestEntityManager em;

    private Fundraiser fundraiser;
    private Fundraiser otherFundraiser;

    @BeforeEach
    void setUp() {
        fundraiser = em.persist(new Fundraiser());
        otherFundraiser = em.persist(new Fundraiser());
    }

    private static Evidence evidence(Fundraiser fundraiser, String receiptNumber, LocalDateTime createdAt) {
        Evidence evidence = new Evidence();
        evidence.setFundraiser(fundraiser);
        evidence.setReceiptNumber(receiptNumber);
        evidence.setCreatedAt(createdAt);
        return evidence;
    }

    @Test
    void saveGeneratesId() {
        Evidence saved = repository.save(evidence(fundraiser, "RCPT-00001", LocalDateTime.now()));

        assertNotNull(saved.getId());
    }

    @Test
    void findAllByFundraiserIdReturnsOnlyEvidencesOfFundraiserOldestFirst() {
        LocalDateTime now = LocalDateTime.now();
        Evidence newer = em.persist(evidence(fundraiser, "RCPT-00002", now));
        Evidence older = em.persist(evidence(fundraiser, "RCPT-00001", now.minusDays(1)));
        em.persist(evidence(otherFundraiser, "RCPT-00003", now));
        em.flush();
        em.clear();

        List<Evidence> result = repository.findAllByFundraiserIdOrderByCreatedAtAsc(fundraiser.getId());

        assertEquals(List.of(older.getId(), newer.getId()), result.stream().map(Evidence::getId).toList());
    }

    @Test
    void deleteByIdRemovesEvidence() {
        Evidence stored = em.persist(evidence(fundraiser, "RCPT-00001", LocalDateTime.now()));

        repository.deleteById(stored.getId());

        assertTrue(repository.findById(stored.getId()).isEmpty());
    }
}
