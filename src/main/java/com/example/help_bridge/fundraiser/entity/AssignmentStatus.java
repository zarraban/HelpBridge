package com.example.help_bridge.fundraiser.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AssignmentStatus {
    ACTIVE("Active"),
    RETURNED("Returned"),
    COMPLETED("Completed");

    private final String name;

    AssignmentStatus(String name){
        this.name = name;
    }
    public boolean canTransitionTo(AssignmentStatus next) {
        return switch (this) {
            case ACTIVE -> next == RETURNED || next == COMPLETED;
            case RETURNED -> next == ACTIVE;
            case COMPLETED -> false;
        };
    }

    @JsonCreator
    public String displayValue(){
        return this.name;
    }
}
