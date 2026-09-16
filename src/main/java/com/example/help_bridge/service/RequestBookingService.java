package com.example.help_bridge.service;

import com.example.help_bridge.model.RequestStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RequestBookingService {

    private final RequestService requestService;
    private final Map<Long, FundraiserEntity> fundraiserStorage = new ConcurrentHashMap<>();
    private final AtomicLong fundraiserIdGenerator = new AtomicLong(1);

    public RequestBookingService(RequestService requestService) {
        this.requestService = requestService;
    }

    public void bookRequest(Long requestId, Long fundId) {
        var entity = requestService.getById(requestId);

        if (entity.status() != RequestStatus.NEW) {
            throw new IllegalStateException("The request is already being processed or closed");
        }

        boolean isFundApproved = checkFundApprovedMock(fundId);
        if (!isFundApproved) {
            throw new IllegalStateException("The fund does not have the APPROVED status and cannot book requests");
        }

        requestService.updateStatus(requestId, RequestStatus.IN_PROGRESS);

        Long fundraiserId = fundraiserIdGenerator.getAndIncrement();
        FundraiserEntity fundraiser = new FundraiserEntity(
                fundraiserId,
                requestId,
                fundId,
                FundraiserStatus.PENDING_ASSIGNMENT
        );
        fundraiserStorage.put(fundraiserId, fundraiser);
    }

    private boolean checkFundApprovedMock(Long fundId) {
        return true;
    }

    public enum FundraiserStatus {
        PENDING_ASSIGNMENT,
        APPROVED,
        REJECTED
    }

    public record FundraiserEntity(
            Long id,
            Long requestId,
            Long fundId,
            FundraiserStatus status
    ) {}
}