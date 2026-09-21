package com.example.help_bridge.volunteer.service;

import com.example.help_bridge.volunteer.dto.request.VolunteerRequest;
import com.example.help_bridge.volunteer.dto.response.VolunteerResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VolunteerService {

    public List<VolunteerResponse> getFundVolunteers(UUID fundId) {
        return List.of(createMockResponse(UUID.randomUUID(), fundId));
    }

    public VolunteerResponse getFundVolunteer(UUID fundId, UUID volunteerId) {
        return createMockResponse(volunteerId, fundId);
    }

    public VolunteerResponse addVolunteer(UUID fundId, VolunteerRequest request) {
        return createMockResponse(UUID.randomUUID(), fundId);
    }

    public VolunteerResponse updateVolunteer(UUID fundId, UUID id, VolunteerRequest request) {
        return createMockResponse(id, fundId);
    }

    public void removeVolunteer(UUID fundId, UUID volunteerId) {
        //mock remove
    }

    private VolunteerResponse createMockResponse(UUID id, UUID fundId) {
        return new VolunteerResponse(
                id,
                fundId,
                "mock",
                "mock",
                "abc@gmail.com",
                "+380123456789");
    }
}
