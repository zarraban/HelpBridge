package com.example.help_bridge.request.entity;

public enum RequestStatus {
    PENDING_VERIFICATION,
    NEW,
    IN_PROGRESS,
    CLOSED;

    public boolean canTransitionTo(RequestStatus next) {
        return switch (this) {
            case PENDING_VERIFICATION -> next == NEW;
            case NEW -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == CLOSED;
            case CLOSED -> false;
        };
    }
}