package com.example.help_bridge.fund.repository;
import com.example.help_bridge.fund.entity.Fund;

import java.util.List;
import java.util.Optional;

public interface FundRepository {
    List<Fund> findAll();
    Optional<Fund> findById(Long id);
    Fund save(Fund fund);
    void deleteById(Long id);
    boolean existsByEdrpou(String edrpou);
}
