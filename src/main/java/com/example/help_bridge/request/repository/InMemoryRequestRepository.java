package com.example.help_bridge.request.repository;

import com.example.help_bridge.request.entity.Request;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRequestRepository implements RequestRepository {

    private final Map<Long, Request> storage = new ConcurrentHashMap<>();

    @Override
    public Request save(Request request) {
        storage.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<Request> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Request> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}