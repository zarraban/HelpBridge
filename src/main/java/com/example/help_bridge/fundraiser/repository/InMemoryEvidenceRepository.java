package com.example.help_bridge.fundraiser.repository;

import com.example.help_bridge.fundraiser.entity.Evidence;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryEvidenceRepository implements EvidenceRepository {

    private final Map<Long, Evidence> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Evidence> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Evidence> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Evidence save(Evidence evidence) {
        evidence.setId(idGenerator.getAndIncrement());
        storage.put(evidence.getId(), evidence);
        return evidence;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
