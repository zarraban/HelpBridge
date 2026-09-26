package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.request.entity.Request;
import com.example.help_bridge.request.entity.RequestStatus;
import com.example.help_bridge.request.event.RequestBookedEvent;
import com.example.help_bridge.request.exception.FundNotApprovedException;
import com.example.help_bridge.request.exception.RequestNotFoundException;
import com.example.help_bridge.request.repository.RequestRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class RequestBookingServiceImpl implements RequestBookingService {

    private final RequestRepository requestRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RequestBookingServiceImpl(RequestRepository requestRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.requestRepository = requestRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RequestResponse bookRequest(Long requestId, Long fundId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!checkFundApprovedMock(fundId)) {
            throw new FundNotApprovedException(fundId);
        }

        request.transitionTo(RequestStatus.IN_PROGRESS);
        Request saved = requestRepository.save(request);

        eventPublisher.publishEvent(new RequestBookedEvent(requestId, fundId));

        return RequestMapper.toResponse(saved);
    }

    private boolean checkFundApprovedMock(Long fundId) {
        return true;
    }
}