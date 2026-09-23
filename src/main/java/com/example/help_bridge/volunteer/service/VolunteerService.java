package com.example.help_bridge.volunteer.service;

import com.example.help_bridge.volunteer.dto.request.VolunteerRequest;
import com.example.help_bridge.volunteer.dto.response.VolunteerResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VolunteerService {

    private final AtomicLong sequence = new AtomicLong(1);

    public List<VolunteerResponse> getFundVolunteers(Long fundId) {
        return List.of(createMockResponse(sequence.getAndIncrement(), fundId));
    }

    public VolunteerResponse getFundVolunteer(Long fundId, Long volunteerId) {
        return createMockResponse(volunteerId, fundId);
    }

    public VolunteerResponse addVolunteer(Long fundId, VolunteerRequest request) {
        return createMockResponse(sequence.getAndIncrement(), fundId);
    }

    public VolunteerResponse updateVolunteer(Long fundId, Long id, VolunteerRequest request) {
        return createMockResponse(id, fundId);
    }

    public void removeVolunteer(Long fundId, Long volunteerId) {
        //mock remove
    }

    private VolunteerResponse createMockResponse(Long id, Long fundId) {
        return new VolunteerResponse(
                id,
                fundId,
                "mock",
                "mock",
                "abc@gmail.com",
                "+380123456789");
    }
}
