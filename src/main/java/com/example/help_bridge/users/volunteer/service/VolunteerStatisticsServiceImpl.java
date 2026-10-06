package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserAssignmentRepository;
import com.example.help_bridge.users.volunteer.dto.request.VolunteerStatisticsRequest;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerStatisticsResponse;
import com.example.help_bridge.users.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VolunteerStatisticsServiceImpl implements VolunteerStatisticsService {

    private final VolunteerRepository volunteerRepository;
    private final FundraiserAssignmentRepository assignmentRepository;

    public VolunteerStatisticsServiceImpl(VolunteerRepository volunteerRepository,
                                          FundraiserAssignmentRepository assignmentRepository) {
        this.volunteerRepository = volunteerRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VolunteerStatisticsResponse getClosedFundraisersStatistics(Long volunteerId, VolunteerStatisticsRequest period) {
        if (!volunteerRepository.existsById(volunteerId)) {
            throw new VolunteerNotFoundException("Volunteer with id " + volunteerId + " not found");
        }

        // Обидві межі періоду включно: [from 00:00, to + 1 день 00:00)
        LocalDateTime start = period.from().atStartOfDay();
        LocalDateTime end = period.to().plusDays(1).atStartOfDay();

        long closed = assignmentRepository.countDistinctFundraisersFinishedBetween(
                volunteerId, AssignmentStatus.COMPLETED, start, end);

        return new VolunteerStatisticsResponse(volunteerId, period.from(), period.to(), closed);
    }
}
