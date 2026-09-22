package com.example.help_bridge.donor.repository;

import com.example.help_bridge.donor.entity.Donor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryDonorRepository implements DonorRepository {

    private final Map<Long, Donor> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Donor> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Donor> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Donor save(Donor donor) {
        donor.setId(idGenerator.getAndIncrement());
        storage.put(donor.getId(), donor);
        return donor;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
