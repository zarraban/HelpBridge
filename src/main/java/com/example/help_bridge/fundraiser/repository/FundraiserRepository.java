package com.example.help_bridge.fundraiser.repository;

import com.example.help_bridge.fundraiser.entity.Fundraiser;
import java.util.List;
import java.util.Optional;

public interface FundraiserRepository {
    List<Fundraiser> findAll();
    Optional<Fundraiser> findById(Long id);
    Fundraiser save(Fundraiser fundraiser);
    void deleteById(Long id);
}
