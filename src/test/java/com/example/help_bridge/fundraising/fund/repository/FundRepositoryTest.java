package com.example.help_bridge.fundraising.fund.repository;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// ВАЖЛИВО: імпорт @DataJpaTest у Boot 4 може відрізнятись, прийми підказку IntelliJ (Alt+Enter)
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class FundRepositoryTest {

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private EntityManager em;

    @Test
    void orphanRemoval_deletesRepresentativeRow() {
        Fund fund = newFund("12345678", 1);
        em.persist(fund);
        em.flush();
        em.clear();

        Fund loaded = fundRepository.findByIdWithRepresentatives(fund.getId()).orElseThrow();
        FundRepresentative rep = loaded.getRepresentatives().iterator().next();
        loaded.removeRepresentative(rep);
        em.flush();
        em.clear();

        Long count = em.createQuery("select count(r) from FundRepresentative r", Long.class)
                .getSingleResult();
        assertThat(count).isZero();
    }

    @Test
    void cascadeRemove_deletesRepresentativesWithFund() {
        Fund fund = newFund("12345678", 2);
        em.persist(fund);
        em.flush();
        em.clear();

        fundRepository.delete(fundRepository.findByIdWithRepresentatives(fund.getId()).orElseThrow());
        em.flush();
        em.clear();

        Long count = em.createQuery("select count(r) from FundRepresentative r", Long.class)
                .getSingleResult();
        assertThat(count).isZero();
    }

    @Test
    void findAllWithRepresentatives_runsSingleQuery() {
        em.persist(newFund("12345678", 3));
        em.persist(newFund("87654321", 3));
        em.flush();
        em.clear();

        Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        fundRepository.findAllWithRepresentatives()
                .forEach(f -> f.getRepresentatives().size());

        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    private Fund newFund(String edrpou, int repsCount) {
        Fund f = new Fund("Фонд " + edrpou, edrpou, "UA123", "Київ", "Київ",
                "+380501112233", edrpou + "@fund.org", "https://fund.org",
                Map.of("Instagram", "https://instagram.com/fund"));
        for (int i = 0; i < repsCount; i++) {
            f.addRepresentative(new FundRepresentative("Дар'я", "Чорна",
                    edrpou + "_" + i + "@mail.com", "+380501112233", "hash", null));
        }
        return f;
    }
}