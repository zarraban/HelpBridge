package com.example.help_bridge.users.fundrepresentative.repository;

import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundRepresentativeRepository extends JpaRepository<FundRepresentative, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<FundRepresentative> findByEmailIgnoreCase(String email);

    @Query("SELECT r FROM FundRepresentative r LEFT JOIN FETCH r.fund")
    List<FundRepresentative> findAllWithFund();

    @Query("SELECT r FROM FundRepresentative r LEFT JOIN FETCH r.fund WHERE r.id = :id")
    Optional<FundRepresentative> findByIdWithFund(@Param("id") Long id);

    @Query("SELECT r FROM FundRepresentative r LEFT JOIN FETCH r.fund f WHERE f.id = :fundId")
    List<FundRepresentative> findByFundIdWithFund(@Param("fundId") Long fundId);
}