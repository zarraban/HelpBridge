package com.example.help_bridge.fundraising.fundraiser.repository;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FundraiserAssignmentRepository extends JpaRepository<FundraiserAssignment, Long> {
    @EntityGraph(attributePaths = "fundraiser")
    @Query("SELECT a FROM FundraiserAssignment a WHERE a.fundraiser.id = :fundraiserId")
    List<FundraiserAssignment> findByFundraiserId(@Param("fundraiserId") Long fundraiserId);
    @EntityGraph(attributePaths = "fundraiser")
    List<FundraiserAssignment> findByVolunteerId(Long volunteerId);
    @Query("""
            SELECT COUNT(a) > 0 FROM FundraiserAssignment a
            WHERE a.fundraiser.id = :fundraiserId AND a.volunteerId = :volunteerId AND a.status = :status
            """)
    boolean existsByFundraiserIdAndVolunteerIdAndStatus(@Param("fundraiserId") Long fundraiserId,
                                                       @Param("volunteerId") Long volunteerId,
                                                       @Param("status") AssignmentStatus status);
    @Query("SELECT COUNT(a) > 0 FROM FundraiserAssignment a WHERE a.fundraiser.id = :fundraiserId AND a.status = :status")
    boolean existsByFundraiserIdAndStatus(@Param("fundraiserId") Long fundraiserId,
                                         @Param("status") AssignmentStatus status);

    @Query("""
            SELECT COUNT(DISTINCT a.fundraiser.id) FROM FundraiserAssignment a
            WHERE a.volunteerId = :volunteerId
              AND a.status = :status
              AND a.finishedAt >= :from AND a.finishedAt < :to
            """)
    long countDistinctFundraisersFinishedBetween(@Param("volunteerId") Long volunteerId,
                                                 @Param("status") AssignmentStatus status,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);
}
