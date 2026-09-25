package com.example.help_bridge.donor.repository;

import com.example.help_bridge.donor.entity.Donor;
import java.util.List;
import java.util.Optional;

public interface DonorRepository {
    List<Donor> findAll();
    List<Donor> findByFundraiserId(Long fundraiserId);
    Optional<Donor> findById(Long id);
    Donor save(Donor donor);
    void deleteById(Long id);
}
