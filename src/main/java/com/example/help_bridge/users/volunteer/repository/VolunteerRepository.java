package com.example.help_bridge.users.volunteer.repository;

import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends ListCrudRepository<Volunteer, Long> {

    Optional<Volunteer> findByFundIdAndIdAndStatus(Long fundId, Long id, VolunteerStatus status);

    Optional<Volunteer> findByFundIdAndEmailIgnoreCase(Long fundId, String email);

    Optional<Volunteer> findByFundIdAndPhone(Long fundId, String phone);

    List<Volunteer> findAllByFundIdAndStatus(Long fundId, VolunteerStatus status);
}