package com.example.help_bridge.fundraiser.repository;

import com.example.help_bridge.fundraiser.entity.Fundraiser;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryFundraiserRepository implements FundraiserRepository {

    private final Map<Long, Fundraiser> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Fundraiser> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Fundraiser> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Fundraiser save(Fundraiser fundraiser) {
        fundraiser.setId(idGenerator.getAndIncrement());
        storage.put(fundraiser.getId(), fundraiser);
        return fundraiser;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
