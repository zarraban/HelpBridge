package com.example.help_bridge.fundraising.fund.repository;
import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class FundNPlusOneTest {

    @Autowired FundRepository fundRepository;
    @Autowired EntityManager em;

    private Fund newFund(int i) {
        return new Fund("Fund " + i, "1234567" + i, "UA00 bank", "Addr", "Addr",
                "+380500000000", "fund" + i + "@mail.com", "https://f" + i + ".org", Map.of());
    }

    private FundRepresentative newRep(String email, Fund fund) {
        return new FundRepresentative("Ім'я", "Прізвище", email, "+380501111111", "hash", fund);
    }

    @Test
    void findAllWithRepresentatives_executesSingleQuery() {
        for (int i = 0; i < 5; i++) {
            Fund fund = newFund(i);
            fund.addRepresentative(newRep("a" + i + "@mail.com", fund));
            fund.addRepresentative(newRep("b" + i + "@mail.com", fund));
            fundRepository.save(fund);
        }
        em.flush();
        em.clear();

        Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        List<Fund> funds = fundRepository.findAllWithRepresentatives();
        funds.forEach(f -> f.getRepresentatives().size());

        assertThat(funds).hasSize(5);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void removeRepresentative_deletesOrphanRow() {
        Fund fund = newFund(1);
        FundRepresentative rep = newRep("a@mail.com", fund);
        fund.addRepresentative(rep);
        fundRepository.saveAndFlush(fund);

        fund.removeRepresentative(rep);
        fundRepository.saveAndFlush(fund);
        em.clear();

        Long count = em.createQuery("select count(r) from FundRepresentative r", Long.class)
                .getSingleResult();
        assertThat(count).isZero();
    }

    @Test
    void deleteFund_cascadesToRepresentatives() {
        Fund fund = newFund(2);
        fund.addRepresentative(newRep("c@mail.com", fund));
        fundRepository.saveAndFlush(fund);

        fundRepository.delete(fund);
        fundRepository.flush();
        em.clear();

        assertThat(em.createQuery("select count(r) from FundRepresentative r", Long.class)
                .getSingleResult()).isZero();
    }
}