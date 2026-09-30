package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.users.volunteer.dto.request.VolunteerStatisticsRequest;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerStatisticsResponse;

public interface VolunteerStatisticsService {
    VolunteerStatisticsResponse getClosedFundraisersStatistics(Long volunteerId, VolunteerStatisticsRequest period);
}
