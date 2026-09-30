package com.example.help_bridge.users.fundrepresentative.repository;

import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundRepresentativeRepository extends JpaRepository<FundRepresentative, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<FundRepresentative> findByEmailIgnoreCase(String email);

    List<FundRepresentative> findByFundId(Long fundId);
}