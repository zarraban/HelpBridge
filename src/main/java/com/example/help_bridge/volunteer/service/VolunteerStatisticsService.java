package com.example.help_bridge.volunteer.service;

import com.example.help_bridge.volunteer.dto.request.VolunteerStatisticsRequest;
import com.example.help_bridge.volunteer.dto.response.VolunteerStatisticsResponse;

public interface VolunteerStatisticsService {
    VolunteerStatisticsResponse getClosedFundraisersStatistics(Long volunteerId, VolunteerStatisticsRequest period);
}
