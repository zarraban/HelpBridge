package com.example.help_bridge.volunteer.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Volunteer {
    private Long id;
    private Long fundId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private VolunteerStatus status;

    public boolean isActive() {
        return status.equals(VolunteerStatus.ACTIVE);
    }
}
