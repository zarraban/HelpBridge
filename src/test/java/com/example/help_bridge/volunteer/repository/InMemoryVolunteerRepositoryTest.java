package com.example.help_bridge.volunteer.repository;

import com.example.help_bridge.volunteer.entity.Volunteer;
import com.example.help_bridge.volunteer.entity.VolunteerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryVolunteerRepositoryTest {

    private static final Long FUND_ID = 1L;
    private static final Long OTHER_FUND_ID = 2L;

    private static final String EMAIL = "anna@gmail.com";
    private static final String PHONE = "+380501234567";

    private InMemoryVolunteerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryVolunteerRepository();
    }

    private static Volunteer newVolunteer(Long fundId, String email, String phone) {
        return new Volunteer(null, fundId, "Anna", "Samana", email, phone, VolunteerStatus.ACTIVE);
    }

    private Volunteer saveActive(Long fundId, String email, String phone) {
        return repository.save(newVolunteer(fundId, email, phone));
    }

    private Volunteer saveInactive(Long fundId, String email, String phone) {
        Volunteer saved = saveActive(fundId, email, phone);
        repository.deleteById(fundId, saved.getId());
        return saved;
    }

    @Nested
    class Save {

        @Test
        void assignsIdToNewVolunteer() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);

            assertNotNull(saved.getId());
            assertTrue(repository.findById(FUND_ID, saved.getId()).isPresent());
        }

        @Test
        void assignsDifferentIdsToDifferentVolunteers() {
            Volunteer first = saveActive(FUND_ID, EMAIL, PHONE);
            Volunteer second = saveActive(FUND_ID, "olena@gmail.com", "+380671112233");

            assertNotEquals(first.getId(), second.getId());
        }

        @Test
        void keepsIdAndOverwritesDataWhenSavingExistingVolunteer() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);
            Long id = saved.getId();

            saved.setFirstName("Hanna");
            saved.setEmail("hanna@gmail.com");
            Volunteer resaved = repository.save(saved);

            assertEquals(id, resaved.getId());
            Volunteer found = repository.findById(FUND_ID, id).orElseThrow();
            assertEquals("Hanna", found.getFirstName());
            assertEquals("hanna@gmail.com", found.getEmail());
            assertEquals(1, repository.findAllByFund(FUND_ID).size());
        }

        @Test
        void concurrentSavesProduceUniqueIdsWithoutLosingData() throws Exception {
            int threads = 8;
            int savesPerThread = 250;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            try {
                List<Callable<List<Long>>> tasks = new ArrayList<>();
                for (int t = 0; t < threads; t++) {
                    int thread = t;
                    tasks.add(() -> {
                        List<Long> ids = new ArrayList<>();
                        for (int i = 0; i < savesPerThread; i++) {
                            String suffix = thread + "-" + i;
                            ids.add(saveActive(FUND_ID, suffix + "@gmail.com", "+38050" + suffix).getId());
                        }
                        return ids;
                    });
                }

                Set<Long> allIds = new HashSet<>();
                for (Future<List<Long>> future : executor.invokeAll(tasks)) {
                    allIds.addAll(future.get());
                }

                assertEquals(threads * savesPerThread, allIds.size());
                assertEquals(threads * savesPerThread, repository.findAllByFund(FUND_ID).size());
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Nested
    class FindById {

        @Test
        void findsActiveVolunteerOfFund() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);

            Optional<Volunteer> found = repository.findById(FUND_ID, saved.getId());

            assertTrue(found.isPresent());
            assertSame(saved, found.get());
        }

        @Test
        void doesNotFindVolunteerOfAnotherFund() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);

            assertTrue(repository.findById(OTHER_FUND_ID, saved.getId()).isEmpty());
        }

        @Test
        void doesNotFindInactiveVolunteer() {
            Volunteer removed = saveInactive(FUND_ID, EMAIL, PHONE);

            assertTrue(repository.findById(FUND_ID, removed.getId()).isEmpty());
        }

        @Test
        void returnsEmptyForUnknownId() {
            assertTrue(repository.findById(FUND_ID, 999L).isEmpty());
        }
    }

    @Nested
    class FindByContacts {

        @Test
        void findsVolunteerByEmailWithinFund() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);
            saveActive(FUND_ID, "olena@gmail.com", "+380671112233");

            assertEquals(saved.getId(), repository.findByEmail(FUND_ID, EMAIL).orElseThrow().getId());
        }

        @Test
        void findsVolunteerByEmailIgnoringCase() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);

            assertEquals(saved.getId(), repository.findByEmail(FUND_ID, "ANNA@Gmail.com").orElseThrow().getId());
        }

        @Test
        void findsVolunteerByPhoneNumberWithinFund() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);
            saveActive(FUND_ID, "olena@gmail.com", "+380671112233");

            assertEquals(saved.getId(), repository.findByPhoneNumber(FUND_ID, PHONE).orElseThrow().getId());
        }

        @Test
        void findsInactiveVolunteerByContactsSoItCanBeReactivated() {
            Volunteer removed = saveInactive(FUND_ID, EMAIL, PHONE);

            Volunteer byEmail = repository.findByEmail(FUND_ID, EMAIL).orElseThrow();
            Volunteer byPhone = repository.findByPhoneNumber(FUND_ID, PHONE).orElseThrow();

            assertEquals(removed.getId(), byEmail.getId());
            assertEquals(VolunteerStatus.INACTIVE, byEmail.getStatus());
            assertEquals(removed.getId(), byPhone.getId());
        }

        @Test
        void doesNotFindContactsOfAnotherFund() {
            saveActive(FUND_ID, EMAIL, PHONE);

            assertTrue(repository.findByEmail(OTHER_FUND_ID, EMAIL).isEmpty());
            assertTrue(repository.findByPhoneNumber(OTHER_FUND_ID, PHONE).isEmpty());
        }

        @Test
        void sameContactsInDifferentFundsBelongToDifferentVolunteers() {
            Volunteer inFund = saveActive(FUND_ID, EMAIL, PHONE);
            Volunteer inOtherFund = saveActive(OTHER_FUND_ID, EMAIL, PHONE);

            assertEquals(inFund.getId(), repository.findByEmail(FUND_ID, EMAIL).orElseThrow().getId());
            assertEquals(inOtherFund.getId(), repository.findByEmail(OTHER_FUND_ID, EMAIL).orElseThrow().getId());
            assertEquals(inFund.getId(), repository.findByPhoneNumber(FUND_ID, PHONE).orElseThrow().getId());
            assertEquals(inOtherFund.getId(), repository.findByPhoneNumber(OTHER_FUND_ID, PHONE).orElseThrow().getId());
        }

        @Test
        void returnsEmptyForUnknownContacts() {
            saveActive(FUND_ID, EMAIL, PHONE);

            assertTrue(repository.findByEmail(FUND_ID, "unknown@gmail.com").isEmpty());
            assertTrue(repository.findByPhoneNumber(FUND_ID, "+380999999999").isEmpty());
        }
    }

    @Nested
    class FindAllByFund {

        @Test
        void returnsOnlyActiveVolunteersOfFund() {
            Volunteer active = saveActive(FUND_ID, EMAIL, PHONE);
            saveInactive(FUND_ID, "olena@gmail.com", "+380671112233");
            saveActive(OTHER_FUND_ID, "petro@gmail.com", "+380931112233");

            List<Volunteer> result = repository.findAllByFund(FUND_ID);

            assertEquals(1, result.size());
            assertEquals(active.getId(), result.getFirst().getId());
        }

        @Test
        void returnsEmptyListForFundWithoutVolunteers() {
            saveActive(FUND_ID, EMAIL, PHONE);

            assertTrue(repository.findAllByFund(OTHER_FUND_ID).isEmpty());
        }
    }

    @Nested
    class Exists {

        @Test
        void existsByIdOnlyForActiveVolunteerOfFund() {
            Volunteer active = saveActive(FUND_ID, EMAIL, PHONE);
            Volunteer inactive = saveInactive(FUND_ID, "olena@gmail.com", "+380671112233");

            assertTrue(repository.existsById(FUND_ID, active.getId()));
            assertFalse(repository.existsById(OTHER_FUND_ID, active.getId()));
            assertFalse(repository.existsById(FUND_ID, inactive.getId()));
            assertFalse(repository.existsById(FUND_ID, 999L));
        }

        @Test
        void existsByContactsCountsInactiveVolunteersToo() {
            saveInactive(FUND_ID, EMAIL, PHONE);

            assertTrue(repository.existsByEmail(FUND_ID, EMAIL));
            assertTrue(repository.existsByPhoneNumber(FUND_ID, PHONE));
        }

        @Test
        void existsByEmailIgnoresCase() {
            saveActive(FUND_ID, EMAIL, PHONE);

            assertTrue(repository.existsByEmail(FUND_ID, "Anna@GMAIL.com"));
        }

        @Test
        void existsByContactsIsScopedToFund() {
            saveActive(FUND_ID, EMAIL, PHONE);

            assertFalse(repository.existsByEmail(OTHER_FUND_ID, EMAIL));
            assertFalse(repository.existsByPhoneNumber(OTHER_FUND_ID, PHONE));
            assertFalse(repository.existsByEmail(FUND_ID, "unknown@gmail.com"));
            assertFalse(repository.existsByPhoneNumber(FUND_ID, "+380999999999"));
        }
    }

    @Nested
    class DeleteById {

        @Test
        void softDeletesVolunteerByMarkingItInactive() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);

            repository.deleteById(FUND_ID, saved.getId());

            assertTrue(repository.findById(FUND_ID, saved.getId()).isEmpty());
            assertTrue(repository.findAllByFund(FUND_ID).isEmpty());
            Volunteer stored = repository.findByEmail(FUND_ID, EMAIL).orElseThrow();
            assertEquals(VolunteerStatus.INACTIVE, stored.getStatus());
        }

        @Test
        void doesNotDeleteVolunteerOfAnotherFund() {
            Volunteer saved = saveActive(FUND_ID, EMAIL, PHONE);

            repository.deleteById(OTHER_FUND_ID, saved.getId());

            assertTrue(repository.findById(FUND_ID, saved.getId()).isPresent());
        }

        @Test
        void deletesOnlyRequestedVolunteer() {
            Volunteer toDelete = saveActive(FUND_ID, EMAIL, PHONE);
            Volunteer toKeep = saveActive(FUND_ID, "olena@gmail.com", "+380671112233");

            repository.deleteById(FUND_ID, toDelete.getId());

            assertTrue(repository.findById(FUND_ID, toKeep.getId()).isPresent());
            assertEquals(1, repository.findAllByFund(FUND_ID).size());
        }

        @Test
        void ignoresUnknownId() {
            assertDoesNotThrow(() -> repository.deleteById(FUND_ID, 999L));
        }

        @Test
        void reactivatedVolunteerIsVisibleAgain() {
            Volunteer removed = saveInactive(FUND_ID, EMAIL, PHONE);

            removed.setStatus(VolunteerStatus.ACTIVE);
            repository.save(removed);

            assertTrue(repository.findById(FUND_ID, removed.getId()).isPresent());
            assertEquals(1, repository.findAllByFund(FUND_ID).size());
        }
    }
}
