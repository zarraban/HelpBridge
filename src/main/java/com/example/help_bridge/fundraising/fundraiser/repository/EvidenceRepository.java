package com.example.help_bridge.fundraising.fundraiser.repository;

import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRepository extends ListCrudRepository<Evidence, Long> {

    List<Evidence> findAllByFundraiserIdOrderByCreatedAtAsc(Long fundraiserId);
}
