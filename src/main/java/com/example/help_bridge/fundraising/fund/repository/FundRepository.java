package com.example.help_bridge.fundraising.fund.repository;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {

    boolean existsByEdrpou(String edrpou);

    List<Fund> findByStatus(FundStatus status);
}