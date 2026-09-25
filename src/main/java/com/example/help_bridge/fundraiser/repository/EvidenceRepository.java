package com.example.help_bridge.fundraiser.repository;

import com.example.help_bridge.fundraiser.entity.Evidence;
import java.util.List;
import java.util.Optional;

public interface EvidenceRepository {
    List<Evidence> findAll();
    Optional<Evidence> findById(Long id);
    Evidence save(Evidence evidence);
    void deleteById(Long id);
}
