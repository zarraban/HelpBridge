package com.example.help_bridge.fundraising.fundraiser.repository;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FundraiserAssignmentRepositoryTest {

    private static final Long VOLUNTEER_ID = 5L;
    private static final LocalDateTime FROM = LocalDateTime.of(2026, 3, 1, 0, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 4, 1, 0, 0);

    @Autowired
    private FundraiserAssignmentRepository repository;

    @Autowired
    private TestEntityManager em;

    private Fundraiser fundraiser;

    @BeforeEach
    void setUp() {
        fundraiser = em.persist(new Fundraiser());
    }

    private void assignment(Fundraiser f, Long volunteerId, AssignmentStatus status, LocalDateTime finishedAt) {
        FundraiserAssignment a = new FundraiserAssignment();
        a.setFundraiser(f);
        a.setVolunteerId(volunteerId);
        a.setStatus(status);
        a.setFinishedAt(finishedAt);
        em.persist(a);
    }

    private long count(Long volunteerId) {
        em.flush();
        em.clear();
        return repository.countDistinctFundraisersFinishedBetween(volunteerId, AssignmentStatus.COMPLETED, FROM, TO);
    }

    @Test
    void countsCompletedAssignmentsInsidePeriod() {
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.COMPLETED, FROM.plusDays(3));
        assignment(em.persist(new Fundraiser()), VOLUNTEER_ID, AssignmentStatus.COMPLETED, FROM);

        assertEquals(2, count(VOLUNTEER_ID));
    }

    @Test
    void ignoresOtherStatusesVolunteersAndDatesOutsidePeriod() {
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.ACTIVE, null);
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.RETURNED, FROM.plusDays(1));
        assignment(fundraiser, 99L, AssignmentStatus.COMPLETED, FROM.plusDays(1));
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.COMPLETED, FROM.minusSeconds(1));
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.COMPLETED, TO);

        assertEquals(0, count(VOLUNTEER_ID));
    }

    @Test
    void countsSameFundraiserOnlyOnce() {
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.COMPLETED, FROM.plusDays(1));
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.COMPLETED, FROM.plusDays(2));

        assertEquals(1, count(VOLUNTEER_ID));
    }

    @Test
    void assignmentQueriesUseFundraiserRelationAndRespectVolunteerAndStatus() {
        assignment(fundraiser, VOLUNTEER_ID, AssignmentStatus.ACTIVE, null);
        var other = em.persist(new Fundraiser());
        assignment(other, VOLUNTEER_ID, AssignmentStatus.RETURNED, FROM);
        Long fundraiserId = fundraiser.getId();
        Long otherId = other.getId();
        em.flush(); em.clear();

        assertEquals(1, repository.findByFundraiserId(fundraiserId).size());
        assertTrue(repository.existsByFundraiserIdAndStatus(fundraiserId, AssignmentStatus.ACTIVE));
        assertFalse(repository.existsByFundraiserIdAndStatus(otherId, AssignmentStatus.ACTIVE));
        assertTrue(repository.existsByFundraiserIdAndVolunteerIdAndStatus(
                fundraiserId, VOLUNTEER_ID, AssignmentStatus.ACTIVE));
        assertFalse(repository.existsByFundraiserIdAndVolunteerIdAndStatus(
                fundraiserId, 99L, AssignmentStatus.ACTIVE));
        assertFalse(repository.existsByFundraiserIdAndVolunteerIdAndStatus(
                otherId, VOLUNTEER_ID, AssignmentStatus.ACTIVE));
    }

}
