package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.users.volunteer.command.RegisterVolunteerCommand;
import com.example.help_bridge.users.volunteer.command.UpdateVolunteerCommand;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerResponse;
import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.event.VolunteerRegisteredEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerRemovedEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedEmailEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedPhoneNumberEvent;
import com.example.help_bridge.users.volunteer.exception.DuplicateVolunteerException;
import com.example.help_bridge.users.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public VolunteerServiceImpl(
            VolunteerRepository repository,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VolunteerResponse> getFundVolunteers(Long fundId) {
        return repository.findAllByFundIdAndStatus(fundId, VolunteerStatus.ACTIVE).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VolunteerResponse getFundVolunteer(Long fundId, Long volunteerId) {
        return repository.findByFundIdAndIdAndStatus(fundId, volunteerId, VolunteerStatus.ACTIVE)
                .map(this::toResponse)
                .orElseThrow(() -> new VolunteerNotFoundException("Volunteer with ID '" + volunteerId + "' not found"));
    }

    @Override
    public VolunteerResponse registerVolunteer(RegisterVolunteerCommand command) {
        String email = normalizeEmail(command.email());
        Optional<Volunteer> byEmail = repository.findByFundIdAndEmailIgnoreCase(command.fundId(), email);
        Optional<Volunteer> byPhone = repository.findByFundIdAndPhone(command.fundId(), command.phone());

        byEmail.filter(Volunteer::isActive).ifPresent(v -> {
            throw new DuplicateVolunteerException("Volunteer with email '" + email + "' already exists");
        });
        byPhone.filter(Volunteer::isActive).ifPresent(v -> {
            throw new DuplicateVolunteerException("Volunteer with phone number '" + command.phone() + "' already exists");
        });

        if (byEmail.isPresent() && byPhone.isPresent()
                && !byEmail.get().getId().equals(byPhone.get().getId())) {
            throw new DuplicateVolunteerException("Email (" + byEmail.get().getId() + ") and phone number (" + byPhone.get().getPhone() + ") belong to different volunteers");
        }

        Volunteer volunteer = byEmail.or(() -> byPhone)
                .orElseGet(() -> {
                    Volunteer v = new Volunteer();
                    v.setFundId(command.fundId());
                    return v;
                });

        volunteer.setFirstName(command.firstName());
        volunteer.setLastName(command.lastName());
        volunteer.setEmail(email);
        volunteer.setPhone(command.phone());
        volunteer.setStatus(VolunteerStatus.ACTIVE);

        Volunteer saved = repository.save(volunteer);
        eventPublisher.publishEvent(new VolunteerRegisteredEvent(
                saved.getFundId(),
                saved.getFirstName(),
                saved.getEmail()));
        return toResponse(saved);
    }

    @Override
    public VolunteerResponse updateVolunteer(UpdateVolunteerCommand command) {
        Volunteer volunteer = repository.findByFundIdAndIdAndStatus(command.fundId(), command.id(), VolunteerStatus.ACTIVE)
                .orElseThrow(() -> new VolunteerNotFoundException("Volunteer with ID '" + command.id() + "' not found"));

        String email = normalizeEmail(command.email());
        repository.findByFundIdAndEmailIgnoreCase(command.fundId(), email)
                .filter(other -> !other.getId().equals(volunteer.getId()))
                .ifPresent(other -> {
                    throw new DuplicateVolunteerException("Volunteer with email '" + email + "' already exists");
                });
        repository.findByFundIdAndPhone(command.fundId(), command.phone())
                .filter(other -> !other.getId().equals(volunteer.getId()))
                .ifPresent(other -> {
                    throw new DuplicateVolunteerException("Volunteer with phone number '" + command.phone() + "' already exists");
                });

        boolean emailChanged = !volunteer.getEmail().equals(email);
        boolean phoneChanged = !volunteer.getPhone().equals(command.phone());

        volunteer.setFirstName(command.firstName());
        volunteer.setLastName(command.lastName());
        volunteer.setEmail(email);
        volunteer.setPhone(command.phone());

        Volunteer saved = repository.save(volunteer);

        if (emailChanged) {
            eventPublisher.publishEvent(new VolunteerUpdatedEmailEvent(saved.getFirstName(), saved.getEmail()));
        }
        if (phoneChanged) {
            eventPublisher.publishEvent(new VolunteerUpdatedPhoneNumberEvent(saved.getFirstName(), saved.getPhone(), saved.getEmail()));
        }
        return toResponse(saved);
    }

    @Override
    public void removeVolunteer(Long fundId, Long volunteerId) {
        Volunteer volunteer = repository.findByFundIdAndIdAndStatus(fundId, volunteerId, VolunteerStatus.ACTIVE)
                .orElseThrow(() -> new VolunteerNotFoundException("Volunteer with ID '" + volunteerId + "' not found"));

        volunteer.setStatus(VolunteerStatus.INACTIVE);
        repository.save(volunteer);

        eventPublisher.publishEvent(new VolunteerRemovedEvent(volunteer.getFirstName(), volunteer.getEmail()));
    }

    private static String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    private VolunteerResponse toResponse(Volunteer v) {
        return new VolunteerResponse(
                v.getId(),
                v.getFundId(),
                v.getFirstName(),
                v.getLastName(),
                v.getEmail(),
                v.getPhone()
        );
    }
}