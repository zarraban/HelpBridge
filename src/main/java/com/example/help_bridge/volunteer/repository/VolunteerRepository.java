package com.example.help_bridge.volunteer.repository;

import com.example.help_bridge.volunteer.entity.Volunteer;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository {
    Volunteer save(Volunteer volunteer);
    Optional<Volunteer> findById(Long fundId, Long id);
    Optional<Volunteer> findByEmail(Long fundId, String email);
    Optional<Volunteer> findByPhoneNumber(Long fundId, String phoneNumber);
    List<Volunteer> findAllByFund(Long fundId);
    boolean existsById(Long fundId, Long id);
    boolean existsByEmail(Long fundId, String email);
    boolean existsByPhoneNumber(Long fundId, String phoneNumber);
    void deleteById(Long fundId, Long id);
}
