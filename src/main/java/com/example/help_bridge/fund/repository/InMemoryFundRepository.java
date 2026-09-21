package com.example.help_bridge.fund.repository;

import com.example.help_bridge.fund.entity.Fund;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryFundRepository implements FundRepository{
    private final Map<Long, Fund> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Fund> findAll(){
        return new ArrayList<>(storage.values());
    }
    @Override
    public Optional<Fund> findById(Long id){
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public  Fund save(Fund fund){
        if (fund.getId() == null){
            fund.setId(idGenerator.getAndIncrement());
        }
        storage.put(fund.getId(), fund);
        return fund;
    }

    @Override
    public  void deleteById(Long id){
        storage.remove(id);
    }
}
