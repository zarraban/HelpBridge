package com.example.help_bridge.fundraising.verification.repository;

import com.example.help_bridge.fundraising.verification.entity.VerificationAct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationActRepository extends JpaRepository<VerificationAct, Long> {
    void deleteByFundId(Long fundId);
}