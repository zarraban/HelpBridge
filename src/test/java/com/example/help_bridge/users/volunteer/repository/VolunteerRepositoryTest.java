package com.example.help_bridge.users.volunteer.repository;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
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

@DataJpaTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
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
        return new Fund(
                name,
                edrpou,
                "UA123456789012345678901234567",
                "Kyiv, Address 1",
                "Kyiv, Address 1",
                "+380441234567",
                "info@fund.org",
                "https://fund.org",
                null
        );
    }

    private Volunteer persist(Long fundId, String email, String phone, VolunteerStatus status) {
        return em.persist(new Volunteer(null, fundId, "Anna", "Samana", email, phone, status));
    }

    @Test
    void saveGeneratesId() {
        Volunteer saved = repository.save(new Volunteer(null, fund.getId(), "Anna", "Samana", EMAIL, PHONE, VolunteerStatus.ACTIVE));

        assertNotNull(saved.getId());
    }

    @Test
    void findAllByFundIdAndStatusReturnsOnlyActiveVolunteersOfFund() {
        Volunteer active = persist(fund.getId(), EMAIL, PHONE, VolunteerStatus.ACTIVE);
        persist(fund.getId(), "inactive@gmail.com", "+380500000001", VolunteerStatus.INACTIVE);
        persist(otherFund.getId(), "other@gmail.com", "+380500000002", VolunteerStatus.ACTIVE);
        em.flush();
        em.clear();

        List<Volunteer> result = repository.findAllByFundIdAndStatus(fund.getId(), VolunteerStatus.ACTIVE);

        assertEquals(List.of(active.getId()), result.stream().map(Volunteer::getId).toList());
    }

    @Test
    void findByFundIdAndIdAndStatusIgnoresInactiveAndOtherFund() {
        Volunteer inactive = persist(fund.getId(), EMAIL, PHONE, VolunteerStatus.INACTIVE);
        Volunteer active = persist(fund.getId(), "active@gmail.com", "+380500000001", VolunteerStatus.ACTIVE);

        assertTrue(repository.findByFundIdAndIdAndStatus(fund.getId(), inactive.getId(), VolunteerStatus.ACTIVE).isEmpty());
        assertTrue(repository.findByFundIdAndIdAndStatus(otherFund.getId(), active.getId(), VolunteerStatus.ACTIVE).isEmpty());
        assertTrue(repository.findByFundIdAndIdAndStatus(fund.getId(), active.getId(), VolunteerStatus.ACTIVE).isPresent());
    }

    @Test
    void findByFundIdAndEmailIgnoreCaseMatchesRegardlessOfCase() {
        Volunteer stored = persist(fund.getId(), EMAIL, PHONE, VolunteerStatus.ACTIVE);

        Optional<Volunteer> found = repository.findByFundIdAndEmailIgnoreCase(fund.getId(), "Anna@Gmail.COM");

        assertEquals(stored.getId(), found.orElseThrow().getId());
        assertTrue(repository.findByFundIdAndEmailIgnoreCase(otherFund.getId(), EMAIL).isEmpty());
    }

    @Test
    void findByFundIdAndPhoneNumberFindsInactiveVolunteerToo() {
        Volunteer inactive = persist(fund.getId(), EMAIL, PHONE, VolunteerStatus.INACTIVE);

        assertEquals(inactive.getId(), repository.findByFundIdAndPhone(fund.getId(), PHONE).orElseThrow().getId());
    }
}