package com.example.help_bridge.users.volunteer.entity;

public enum VolunteerStatus {
    ACTIVE,
    INACTIVE;

    /*
    public boolean canTransitionTo(VolunteerStatus next) {
        return switch (this) {
            case ACTIVE -> next == INACTIVE;
            default -> false;
        };
    }
     */
}
