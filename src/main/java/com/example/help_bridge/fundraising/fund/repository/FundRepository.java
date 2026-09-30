package com.example.help_bridge.fundraising.fund.repository;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {

    boolean existsByEdrpou(String edrpou);

    List<Fund> findByStatus(FundStatus status);

    @Query("SELECT DISTINCT f FROM Fund f LEFT JOIN FETCH f.representatives")
    List<Fund> findAllWithRepresentatives();

    @Query("SELECT f FROM Fund f LEFT JOIN FETCH f.representatives WHERE f.id = :id")
    Optional<Fund> findByIdWithRepresentatives(@Param("id") Long id);

}