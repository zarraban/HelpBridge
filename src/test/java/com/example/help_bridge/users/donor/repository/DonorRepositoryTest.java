package com.example.help_bridge.users.donor.repository;

import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.users.donor.entity.Donor;
import org.hibernate.Hibernate;
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
class DonorRepositoryTest {

    @Autowired
    private DonorRepository repository;

    @Autowired
    private TestEntityManager em;

    private Fundraiser fundraiser;
    private Fundraiser otherFundraiser;

    @BeforeEach
    void setUp() {
        fundraiser = em.persist(new Fundraiser());
        otherFundraiser = em.persist(new Fundraiser());
    }

    private static Donor donor(Fundraiser fundraiser, String email) {
        Donor donor = new Donor();
        donor.setFundraiser(fundraiser);
        donor.setFirstName("Jane");
        donor.setLastName("Doe");
        donor.setEmail(email);
        donor.setPhone("+380501234567");
        donor.setCreatedAt(LocalDateTime.now());
        return donor;
    }

    @Test
    void saveGeneratesId() {
        Donor saved = repository.save(donor(fundraiser, "jane@example.com"));

        assertNotNull(saved.getId());
    }

    @Test
    void saveOfExistingDonorUpdatesInsteadOfCreatingDuplicate() {
        Donor saved = repository.save(donor(fundraiser, "jane@example.com"));

        saved.setFirstName("Janet");
        repository.save(saved);
        em.flush();
        em.clear();

        assertEquals(1, repository.count());
        assertEquals("Janet", repository.findById(saved.getId()).orElseThrow().getFirstName());
    }

    @Test
    void findAllByFundraiserIdWithFundraiserReturnsOnlyDonorsOfFundraiserWithFundraiserLoaded() {
        Donor own = em.persist(donor(fundraiser, "jane@example.com"));
        em.persist(donor(otherFundraiser, "other@example.com"));
        em.flush();
        em.clear();

        List<Donor> result = repository.findAllByFundraiserIdWithFundraiser(fundraiser.getId());

        assertEquals(List.of(own.getId()), result.stream().map(Donor::getId).toList());
        // JOIN FETCH: збір завантажено тим самим запитом, а не лінивим проксі
        assertTrue(Hibernate.isInitialized(result.getFirst().getFundraiser()));
    }

    @Test
    void deleteByIdRemovesDonor() {
        Donor stored = em.persist(donor(fundraiser, "jane@example.com"));

        repository.deleteById(stored.getId());

        assertTrue(repository.findById(stored.getId()).isEmpty());
    }
}
