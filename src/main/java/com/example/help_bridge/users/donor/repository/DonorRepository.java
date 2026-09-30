package com.example.help_bridge.users.donor.repository;

import com.example.help_bridge.users.donor.entity.Donor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends ListCrudRepository<Donor, Long> {

    // JOIN FETCH підтягує збір тим самим запитом — без N+1 і LazyInitializationException
    @Query("""
            SELECT d FROM Donor d
            JOIN FETCH d.fundraiser f
            WHERE f.id = :fundraiserId
            """)
    List<Donor> findAllByFundraiserIdWithFundraiser(@Param("fundraiserId") Long fundraiserId);
}
