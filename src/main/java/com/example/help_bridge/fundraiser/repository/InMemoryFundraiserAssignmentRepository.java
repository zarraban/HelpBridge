package com.example.help_bridge.fundraiser.repository;

import com.example.help_bridge.fundraiser.entity.FundraiserAssignment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryFundraiserAssignmentRepository implements FundraiserAssignmentRepository {

    private final Map<Long, FundraiserAssignment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<FundraiserAssignment> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<FundraiserAssignment> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public FundraiserAssignment save(FundraiserAssignment fundraiserAssignment) {
        fundraiserAssignment.setId(idGenerator.getAndIncrement());
        storage.put(fundraiserAssignment.getId(), fundraiserAssignment);
        return fundraiserAssignment;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
