package com.example.help_bridge.users.fundrepresentative.repository;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class FundRepresentativeRepositoryTest {

    @Autowired
    private FundRepresentativeRepository repository;

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private EntityManager em;

    private Fund savedFund(String edrpou) {
        return fundRepository.saveAndFlush(new Fund("Fund " + edrpou, edrpou, "UA00", "Addr", "Addr",
                "+380500000000", edrpou + "@fund.org", "https://fund.org", Map.of()));
    }

    private FundRepresentative savedRep(String email, Fund fund) {
        return repository.saveAndFlush(new FundRepresentative(
                "Daria", "Chorna", email, "+380501111111", "hash", fund));
    }

    @Test
    void existsByEmail_andExistsByEmailAndIdNot_work() {
        FundRepresentative rep = savedRep("chorna@mail.com", savedFund("12345678"));

        assertThat(repository.existsByEmail("chorna@mail.com")).isTrue();
        assertThat(repository.existsByEmail("other@mail.com")).isFalse();
        assertThat(repository.existsByEmailAndIdNot("chorna@mail.com", rep.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("chorna@mail.com", rep.getId() + 100)).isTrue();
    }

    @Test
    void findByEmailIgnoreCase_ignoresCase() {
        Fund fund = savedFund("12345678");
        FundRepresentative rep = savedRep("chorna@mail.com", fund);
        em.clear();

        assertThat(repository.findByEmailIgnoreCase("chorna@mail.com"))
                .get()
                .extracting(FundRepresentative::getId)
                .isEqualTo(rep.getId());
        assertThat(repository.findByEmailIgnoreCase("CHORNA@MAIL.COM")).isPresent();
        assertThat(repository.findByEmailIgnoreCase("none@mail.com")).isEmpty();
    }

    @Test
    void save_duplicateEmail_violatesUniqueConstraint() {
        Fund fund = savedFund("12345678");
        savedRep("chorna@mail.com", fund);

        assertThatThrownBy(() -> savedRep("chorna@mail.com", fund))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllWithFund_returnsAllRepresentatives() {
        Fund first = savedFund("12345678");
        Fund second = savedFund("87654321");
        savedRep("a@mail.com", first);
        savedRep("b@mail.com", second);
        em.clear();

        assertThat(repository.findAllWithFund()).hasSize(2);
    }

    @Test
    void findByIdWithFund_returnsRepresentativeWithFund() {
        Fund fund = savedFund("12345678");
        FundRepresentative rep = savedRep("chorna@mail.com", fund);
        em.clear();

        assertThat(repository.findByIdWithFund(rep.getId()))
                .get()
                .extracting(r -> r.getFund().getId())
                .isEqualTo(fund.getId());
        assertThat(repository.findByIdWithFund(rep.getId() + 100)).isEmpty();
    }

    @Test
    void findByFundIdWithFund_returnsOnlyThatFundsRepresentatives() {
        Fund first = savedFund("12345678");
        Fund second = savedFund("87654321");
        savedRep("a@mail.com", first);
        savedRep("b@mail.com", first);
        savedRep("c@mail.com", second);
        em.clear();

        assertThat(repository.findByFundIdWithFund(first.getId())).hasSize(2);
        assertThat(repository.findByFundIdWithFund(second.getId())).hasSize(1);
        assertThat(repository.findByFundIdWithFund(first.getId() + 100)).isEmpty();
    }
}