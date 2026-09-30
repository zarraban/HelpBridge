package com.example.help_bridge.fundraising.fund.repository;
import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FundNPlusOneTest {

    @Autowired FundRepository fundRepository;
    @Autowired FundRepresentativeRepository representativeRepository;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;

    @Test
    void findAllWithRepresentatives_executesSingleQuery() {
        for (int i = 0; i < 3; i++) {
            Fund fund = fundRepository.save(new Fund( "Fund" + i,
                    String.format("%08d", i), "bank", "addr", "addr",
                    "+380000000", "f" + i + "@mail.com", "site", Map.of()));
            for (int j = 0; j < 2; j++) {
                representativeRepository.save(new FundRepresentative(
                        "A", "B", "r" + i + j + "@mail.com", "+38000", "hash12345", fund));
            }
        }
        em.flush();
        em.clear();

        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        List<Fund> funds = fundRepository.findAllWithRepresentatives();
        funds.forEach(f -> assertThat(f.getRepresentatives()).hasSize(2));

        assertThat(funds).hasSize(3);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }
}