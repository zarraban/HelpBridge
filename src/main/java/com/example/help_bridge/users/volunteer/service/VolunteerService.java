package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.users.volunteer.command.RegisterVolunteerCommand;
import com.example.help_bridge.users.volunteer.command.UpdateVolunteerCommand;
import com.example.help_bridge.users.volunteer.dto.request.VolunteerRequest;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerResponse;

import java.util.List;

public interface VolunteerService {

    List<VolunteerResponse> getFundVolunteers(Long fundId);
    VolunteerResponse getFundVolunteer(Long fundId, Long volunteerId);
    VolunteerResponse registerVolunteer(RegisterVolunteerCommand command);
    VolunteerResponse updateVolunteer(UpdateVolunteerCommand command);
    void removeVolunteer(Long fundId, Long volunteerId);
}
