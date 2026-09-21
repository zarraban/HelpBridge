package com.example.help_bridge.fund.entity;

public enum FundStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED;

    public boolean canTransitionTo(FundStatus next){
        return switch (this){
            case PENDING_APPROVAL -> next == APPROVED || next == REJECTED;
            case APPROVED, REJECTED -> false;
        };
    }
}
