package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.event.RequestBookedEvent;
import com.example.help_bridge.fundraising.request.exception.FundNotApprovedException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestBookingServiceImpl implements RequestBookingService {

    private final RequestRepository requestRepository;
    private final FundRepository fundRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RequestBookingServiceImpl(RequestRepository requestRepository,
                                     FundRepository fundRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.requestRepository = requestRepository;
        this.fundRepository = fundRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RequestResponse bookRequest(Long requestId, Long fundId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));

        if (!isFundApproved(fund)) {
            throw new FundNotApprovedException(fundId);
        }

        request.transitionTo(RequestStatus.IN_PROGRESS);
        request.setFund(fund);
        Request saved = requestRepository.save(request);

        eventPublisher.publishEvent(new RequestBookedEvent(requestId, fundId));

        return RequestMapper.toResponse(saved);
    }

    private boolean isFundApproved(Fund fund) {
        return true;
    }
}