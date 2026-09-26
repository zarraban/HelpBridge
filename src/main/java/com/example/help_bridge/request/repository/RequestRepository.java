package com.example.help_bridge.request.repository;

import com.example.help_bridge.request.entity.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository {
    Request save(Request request);
    Optional<Request> findById(Long id);
    List<Request> findAll();
    void deleteById(Long id);
}