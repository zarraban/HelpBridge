package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.users.volunteer.dto.request.VolunteerStatisticsRequest;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerStatisticsResponse;
import org.springframework.stereotype.Service;


@Service
public class VolunteerStatisticsServiceImpl implements VolunteerStatisticsService {
    @Override
    public VolunteerStatisticsResponse getClosedFundraisersStatistics(Long volunteerId, VolunteerStatisticsRequest period) {

        return new VolunteerStatisticsResponse(volunteerId, period.from(), period.to(), 100);
    }
}

