package com.example.help_bridge.fundraising.fundraiser.repository;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundraiserAssignmentRepository extends JpaRepository<FundraiserAssignment, Long> {
    List<FundraiserAssignment> findByFundraiserId(Long fundraiserId);
    List<FundraiserAssignment> findByVolunteerId(Long volunteerId);
}