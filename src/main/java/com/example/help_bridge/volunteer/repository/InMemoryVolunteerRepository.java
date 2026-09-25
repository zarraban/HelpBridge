package com.example.help_bridge.volunteer.repository;

import com.example.help_bridge.volunteer.entity.Volunteer;
import com.example.help_bridge.volunteer.entity.VolunteerStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryVolunteerRepository implements VolunteerRepository {
    private final Map<Long, Volunteer> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Volunteer save(Volunteer volunteer) {
        if (volunteer.getId() == null) {
            volunteer.setId(sequence.getAndIncrement());
        }
        storage.put(volunteer.getId(), volunteer);
        return volunteer;
    }

    @Override
    public Optional<Volunteer> findById(Long fundId, Long id) {
        Volunteer volunteer = storage.get(id);
        if (volunteer != null && volunteer.isActive() && volunteer.getFundId().equals(fundId)) {
            return Optional.of(volunteer);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Volunteer> findByEmail(Long fundId, String email) {
        return storage.values().stream()
                .filter(v ->
                        v.getFundId().equals(fundId)
                        && v.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<Volunteer> findByPhoneNumber(Long fundId, String phoneNumber) {
        return storage.values().stream()
                .filter(v ->
                        v.getFundId().equals(fundId)
                        && v.getPhoneNumber().equals(phoneNumber))
                .findFirst();
    }

    @Override
    public List<Volunteer> findAllByFund(Long fundId) {
        return storage.values().stream()
                .filter(v ->
                        v.getStatus().equals(VolunteerStatus.ACTIVE)
                        && v.getFundId().equals(fundId))
                .toList();
    }

    @Override
    public boolean existsById(Long fundId, Long id) {
        return storage.values().stream()
                .anyMatch(v ->
                        v.isActive()
                        && v.getFundId().equals(fundId)
                        && v.getId().equals(id));
    }

    @Override
    public boolean existsByEmail(Long fundId, String email) {
        return storage.values().stream()
                .anyMatch(v ->
                        v.getFundId().equals(fundId)
                        && v.getEmail().equals(email));
    }

    @Override
    public boolean existsByPhoneNumber(Long fundId, String phoneNumber) {
        return storage.values().stream()
                .anyMatch(v ->
                        v.getFundId().equals(fundId)
                        && v.getPhoneNumber().equals(phoneNumber));
    }

    @Override
    public void deleteById(Long fundId, Long id) {
        Volunteer volunteer = storage.get(id);
        if (volunteer != null && volunteer.getFundId().equals(fundId)) {
            volunteer.setStatus(VolunteerStatus.INACTIVE);
        }
    }
}
