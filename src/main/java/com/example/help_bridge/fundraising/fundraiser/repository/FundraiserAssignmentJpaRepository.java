package com.example.help_bridge.fundraising.fundraiser.repository;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundraiserAssignmentJpaRepository extends JpaRepository<FundraiserAssignment, Long> {
}