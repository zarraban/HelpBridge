package com.example.help_bridge.users.volunteer.exception;

public class VolunteerHasActiveAssignmentsException extends RuntimeException {
    public VolunteerHasActiveAssignmentsException() {
        super("Спочатку завершіть або поверніть активні призначення волонтера.");
    }
}
