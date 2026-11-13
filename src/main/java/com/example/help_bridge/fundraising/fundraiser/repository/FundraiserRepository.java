package com.example.help_bridge.fundraising.fundraiser.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface FundraiserRepository extends JpaRepository<Fundraiser, Long> {
    @Query("SELECT f FROM Fundraiser f WHERE f.requestId IN " +
            "(SELECT r.id FROM Request r WHERE r.fund.id = :fundId)")
    List<Fundraiser> findAllByFundId(@Param("fundId") Long fundId);

    @Query(value = "SELECT f FROM Fundraiser f WHERE f.requestId IN " +
            "(SELECT r.id FROM Request r WHERE r.fund.id = :fundId)",
            countQuery = "SELECT COUNT(f) FROM Fundraiser f WHERE f.requestId IN " +
                    "(SELECT r.id FROM Request r WHERE r.fund.id = :fundId)")
    Page<Fundraiser> findAllByFundId(
            @Param("fundId") Long fundId, Pageable pageable);

    boolean existsByRequestId(Long requestId);
}