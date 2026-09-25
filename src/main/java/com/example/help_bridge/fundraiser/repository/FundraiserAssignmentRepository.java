package com.example.help_bridge.fundraiser.repository;

import com.example.help_bridge.fundraiser.entity.FundraiserAssignment;
import java.util.List;
import java.util.Optional;

public interface FundraiserAssignmentRepository {
    List<FundraiserAssignment> findAll();
    Optional<FundraiserAssignment> findById(Long id);
    FundraiserAssignment save(FundraiserAssignment fundraiserAssignment);
    void deleteById(Long id);
}
