package com.example.help_bridge.users.volunteer.repository;

import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends ListCrudRepository<Volunteer, Long> {

    Optional<Volunteer> findByFundIdAndIdAndStatus(Long fundId, Long id, VolunteerStatus status);

    Optional<Volunteer> findByFundIdAndEmailIgnoreCase(Long fundId, String email);

    Optional<Volunteer> findByFundIdAndPhone(Long fundId, String phone);

    @Query("""
            SELECT v FROM Volunteer v
            JOIN FETCH v.fund f
            WHERE f.id = :fundId AND v.status = :status
            """)
    List<Volunteer> findAllByFundWithFund(@Param("fundId") Long fundId,
                                          @Param("status") VolunteerStatus status);
}
