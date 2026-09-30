package com.example.help_bridge.users.donor.repository;

import com.example.help_bridge.users.donor.entity.Donor;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends ListCrudRepository<Donor, Long> {

    List<Donor> findAllByFundraiserId(Long fundraiserId);
}