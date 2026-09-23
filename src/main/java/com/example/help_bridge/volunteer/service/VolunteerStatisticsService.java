package com.example.help_bridge.volunteer.service;

import com.example.help_bridge.volunteer.dto.request.VolunteerStatisticsRequest;
import com.example.help_bridge.volunteer.dto.response.VolunteerStatisticsResponse;
import org.springframework.stereotype.Service;

@Service
public class VolunteerStatisticsService {

    public VolunteerStatisticsResponse getClosedFundraisersStatistics(Long volunteerId, VolunteerStatisticsRequest period) {

        return new VolunteerStatisticsResponse(volunteerId, period.from(), period.to(), 100);
    }
}
