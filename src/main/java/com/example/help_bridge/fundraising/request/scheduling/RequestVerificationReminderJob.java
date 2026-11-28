package com.example.help_bridge.fundraising.request.scheduling;

import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class RequestVerificationReminderJob {

    private final RequestRepository requestRepository;
    private final Duration pendingAge;
    private final String instanceId;

    RequestVerificationReminderJob(RequestRepository requestRepository,
                                   @Value("${helpbridge.scheduling.request-reminder.pending-age:PT24H}")
                                   Duration pendingAge,
                                   @Value("${helpbridge.scheduling.instance-id:${HOSTNAME:localhost}:${server.port:8080}}")
                                   String instanceId) {
        this.requestRepository = requestRepository;
        this.pendingAge = pendingAge;
        this.instanceId = instanceId;
    }

    @Scheduled(cron = "${helpbridge.scheduling.request-reminder.cron:0 5 * * * *}", zone = "UTC")
    @SchedulerLock(name = "request-verification-reminder",
            lockAtLeastFor = "${helpbridge.scheduling.request-reminder.lock-at-least-for:PT30S}")
    public void remind() {
        LockAssert.assertLocked();
        LocalDateTime cutoff = LocalDateTime.ofInstant(Instant.now().minus(pendingAge), ZoneId.systemDefault());
        long overdueCount = requestRepository.countByStatusAndCreatedAtBefore(RequestStatus.PENDING_VERIFICATION, cutoff);

        if (overdueCount > 0) {
            log.info("job=request-verification-reminder instance={} overdueCount={} cutoff={} reviewPath=/requests",
                    instanceId, overdueCount, cutoff);
        } else {
            log.debug("job=request-verification-reminder instance={} overdueCount=0 cutoff={}", instanceId, cutoff);
        }
    }
}
