package com.example.help_bridge.fundraising.fundraiser.repository;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FundraiserAssignmentRepository extends JpaRepository<FundraiserAssignment, Long> {
    List<FundraiserAssignment> findByFundraiserId(Long fundraiserId);
    List<FundraiserAssignment> findByVolunteerId(Long volunteerId);

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
