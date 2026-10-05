package com.example.help_bridge.users.volunteer.repository;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class VolunteerRepositoryTest {

    private static final String EMAIL = "anna@gmail.com";
    private static final String PHONE = "+380501234567";

    @Autowired
    private VolunteerRepository repository;

    @Autowired
    private TestEntityManager em;

    private Fund fund;
    private Fund otherFund;

    @BeforeEach
    void setUp() {
        fund = em.persist(fund("Fund A", "11111111"));
        otherFund = em.persist(fund("Fund B", "22222222"));
    }

    private static Fund fund(String name, String edrpou) {
        return new Fund(name, edrpou, "IBAN123", "Reg address", "Act address", "+380501112233", "f@mail.com", "https://f.com", java.util.Map.of());
    }

    private Volunteer persist(Fund fund, String email, String phone, VolunteerStatus status) {
        return em.persist(new Volunteer(null, fund, "Anna", "Samana", email, phone, status));
    }

    @Test
    void saveGeneratesId() {
        Volunteer saved = repository.save(new Volunteer(null, fund, "Anna", "Samana", EMAIL, PHONE, VolunteerStatus.ACTIVE));

        assertNotNull(saved.getId());
    }

    @Test
    void findAllByFundWithFundReturnsOnlyActiveVolunteersOfFundWithFundLoaded() {
        Volunteer active = persist(fund, EMAIL, PHONE, VolunteerStatus.ACTIVE);
        persist(fund, "inactive@gmail.com", "+380500000001", VolunteerStatus.INACTIVE);
        persist(otherFund, "other@gmail.com", "+380500000002", VolunteerStatus.ACTIVE);
        em.flush();
        em.clear();

        List<Volunteer> result = repository.findAllByFundWithFund(fund.getId(), VolunteerStatus.ACTIVE);

        assertEquals(List.of(active.getId()), result.stream().map(Volunteer::getId).toList());
        assertTrue(Hibernate.isInitialized(result.getFirst().getFund()));
    }

    @Test
    void findByFundIdAndIdAndStatusIgnoresInactiveAndOtherFund() {
        Volunteer inactive = persist(fund, EMAIL, PHONE, VolunteerStatus.INACTIVE);
        Volunteer active = persist(fund, "active@gmail.com", "+380500000001", VolunteerStatus.ACTIVE);

        assertTrue(repository.findByFundIdAndIdAndStatus(fund.getId(), inactive.getId(), VolunteerStatus.ACTIVE).isEmpty());
        assertTrue(repository.findByFundIdAndIdAndStatus(otherFund.getId(), active.getId(), VolunteerStatus.ACTIVE).isEmpty());
        assertTrue(repository.findByFundIdAndIdAndStatus(fund.getId(), active.getId(), VolunteerStatus.ACTIVE).isPresent());
    }

    @Test
    void findByFundIdAndEmailIgnoreCaseMatchesRegardlessOfCase() {
        Volunteer stored = persist(fund, EMAIL, PHONE, VolunteerStatus.ACTIVE);

        Optional<Volunteer> found = repository.findByFundIdAndEmailIgnoreCase(fund.getId(), "Anna@Gmail.COM");

        assertEquals(stored.getId(), found.orElseThrow().getId());
        assertTrue(repository.findByFundIdAndEmailIgnoreCase(otherFund.getId(), EMAIL).isEmpty());
    }

    @Test
    void findByFundIdAndPhoneNumberFindsInactiveVolunteerToo() {
        Volunteer inactive = persist(fund, EMAIL, PHONE, VolunteerStatus.INACTIVE);

        assertEquals(inactive.getId(), repository.findByFundIdAndPhone(fund.getId(), PHONE).orElseThrow().getId());
    }
}
