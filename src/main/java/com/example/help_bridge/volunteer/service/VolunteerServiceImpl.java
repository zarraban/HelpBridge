package com.example.help_bridge.volunteer.service;

import com.example.help_bridge.volunteer.command.RegisterVolunteerCommand;
import com.example.help_bridge.volunteer.command.UpdateVolunteerCommand;
import com.example.help_bridge.volunteer.dto.response.VolunteerResponse;
import com.example.help_bridge.volunteer.entity.Volunteer;
import com.example.help_bridge.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.volunteer.event.VolunteerRegisteredEvent;
import com.example.help_bridge.volunteer.event.VolunteerRemovedEvent;
import com.example.help_bridge.volunteer.event.VolunteerUpdatedEmailEvent;
import com.example.help_bridge.volunteer.event.VolunteerUpdatedPhoneNumberEvent;
import com.example.help_bridge.volunteer.exception.DuplicateVolunteerException;
import com.example.help_bridge.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.volunteer.repository.VolunteerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
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
    public List<VolunteerResponse> getFundVolunteers(Long fundId) {
        return repository.findAllByFund(fundId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public VolunteerResponse getFundVolunteer(Long fundId, Long volunteerId) {
        return repository.findById(fundId, volunteerId)
                .map(this::toResponse)
                .orElseThrow(() -> new VolunteerNotFoundException("Volunteer with ID '" + volunteerId + "' not found"));
    }

    @Override
    public VolunteerResponse registerVolunteer(RegisterVolunteerCommand command) {
        String email = normalizeEmail(command.email());
        Optional<Volunteer> byEmail = repository.findByEmail(command.fundId(), email);
        Optional<Volunteer> byPhone = repository.findByPhoneNumber(command.fundId(), command.phoneNumber());

        byEmail.filter(Volunteer::isActive).ifPresent(v -> {
            throw new DuplicateVolunteerException("Volunteer with email '" + email + "' already exists");
        });
        byPhone.filter(Volunteer::isActive).ifPresent(v -> {
            throw new DuplicateVolunteerException("Volunteer with phone number '" + command.phoneNumber() + "' already exists");
        });

        // email і телефон належать двом різним неактивним записам — не можна однозначно відновити
        if (byEmail.isPresent() && byPhone.isPresent()
                && !byEmail.get().getId().equals(byPhone.get().getId())) {
            throw new DuplicateVolunteerException("Email (" + byEmail.get().getId() + ") and phone number (" + byPhone.get().getPhoneNumber() + ") belong to different volunteers");
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
        volunteer.setPhoneNumber(command.phoneNumber());
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
        Volunteer volunteer = repository.findById(command.fundId(), command.id())
                .orElseThrow(() -> new VolunteerNotFoundException("Volunteer with ID '" + command.id() + "' not found"));

        String email = normalizeEmail(command.email());
        repository.findByEmail(command.fundId(), email)
                .filter(other -> !other.getId().equals(volunteer.getId()))
                .ifPresent(other -> {
                    throw new DuplicateVolunteerException("Volunteer with email '" + email + "' already exists");
                });
        repository.findByPhoneNumber(command.fundId(), command.phoneNumber())
                .filter(other -> !other.getId().equals(volunteer.getId()))
                .ifPresent(other -> {
                    throw new DuplicateVolunteerException("Volunteer with phone number '" + command.phoneNumber() + "' already exists");
                });

        boolean emailChanged = !volunteer.getEmail().equals(email);
        boolean phoneChanged = !volunteer.getPhoneNumber().equals(command.phoneNumber());

        volunteer.setFirstName(command.firstName());
        volunteer.setLastName(command.lastName());
        volunteer.setEmail(email);
        volunteer.setPhoneNumber(command.phoneNumber());

        Volunteer saved = repository.save(volunteer);

        if (emailChanged) {
            eventPublisher.publishEvent(new VolunteerUpdatedEmailEvent(saved.getFirstName(), saved.getEmail()));
        }
        if (phoneChanged) {
            eventPublisher.publishEvent(new VolunteerUpdatedPhoneNumberEvent(saved.getFirstName(), saved.getPhoneNumber(), saved.getEmail()));
        }
        return toResponse(saved);
    }

    @Override
    public void removeVolunteer(Long fundId, Long volunteerId) {
        Volunteer volunteer = repository.findById(fundId, volunteerId)
                .orElseThrow(() -> new VolunteerNotFoundException("Volunteer with ID '" + volunteerId + "' not found"));

        repository.deleteById(fundId, volunteerId);

        eventPublisher.publishEvent(new VolunteerRemovedEvent(volunteer.getFirstName(), volunteer.getEmail()));
    }

    // Email не залежить від регістру — зберігаємо й порівнюємо в нижньому регістрі
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
                v.getPhoneNumber()
        );
    }
}
