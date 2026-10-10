package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.fundraising.fundraiser.spi.VolunteerDirectory;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.springframework.stereotype.Component;

@Component
class VolunteerDirectoryImpl implements VolunteerDirectory {

    private final VolunteerRepository volunteerRepository;

    VolunteerDirectoryImpl(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    @Override
    public void requireActiveVolunteer(Long volunteerId) {
        if (!volunteerRepository.existsByIdAndStatus(volunteerId, VolunteerStatus.ACTIVE)) {
            throw new VolunteerNotFoundException("Volunteer with ID '" + volunteerId + "' not found");
        }
    }
}
