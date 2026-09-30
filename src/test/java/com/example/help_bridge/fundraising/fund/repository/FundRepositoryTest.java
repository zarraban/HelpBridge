package com.example.help_bridge.fundraising.fund.repository;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FundRepositoryTest {

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private EntityManager em;

    @Test
    void deleteFund_deletesFundRow() {
        Fund fund = newFund("12345678");
        em.persist(fund);
        em.flush();
        em.clear();

        fundRepository.delete(fundRepository.findById(fund.getId()).orElseThrow());
        em.flush();
        em.clear();

        assertThat(fundRepository.findById(fund.getId())).isEmpty();
    }

    @Test
    void findAll_runsSingleQuery() {
        em.persist(newFund("12345678"));
        em.persist(newFund("87654321"));
        em.flush();
        em.clear();

        Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        fundRepository.findAll();

        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    private Fund newFund(String edrpou) {
        return new Fund("Фонд " + edrpou, edrpou, "UA123", "Київ", "Київ",
                "+380501112233", edrpou + "@fund.org", "https://fund.org",
                Map.of("Instagram", "https://instagram.com/fund"));
    }
}