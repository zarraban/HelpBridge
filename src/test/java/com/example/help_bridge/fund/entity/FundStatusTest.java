package com.example.help_bridge.fund.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FundStatusTest {

    @Test
    void pendingApproval_canTransitionToApprovedOrRejected() {
        assertThat(FundStatus.PENDING_APPROVAL.canTransitionTo(FundStatus.APPROVED)).isTrue();
        assertThat(FundStatus.PENDING_APPROVAL.canTransitionTo(FundStatus.REJECTED)).isTrue();
    }

    @Test
    void pendingApproval_cannotTransitionToItself() {
        assertThat(FundStatus.PENDING_APPROVAL.canTransitionTo(FundStatus.PENDING_APPROVAL)).isFalse();
    }

    @Test
    void approvedAndRejected_areTerminal() {
        for (FundStatus target : FundStatus.values()) {
            assertThat(FundStatus.APPROVED.canTransitionTo(target)).isFalse();
            assertThat(FundStatus.REJECTED.canTransitionTo(target)).isFalse();
        }
    }
}
