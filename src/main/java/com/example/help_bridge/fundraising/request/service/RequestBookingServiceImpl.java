package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.event.RequestBookedEvent;
import com.example.help_bridge.fundraising.request.exception.FundNotApprovedException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestBookingServiceImpl implements RequestBookingService {

    private final RequestRepository requestRepository;
    private final FundRepository fundRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestAccessPolicy access;

    public RequestBookingServiceImpl(RequestRepository requestRepository,
                                     FundRepository fundRepository,
                                     ApplicationEventPublisher eventPublisher, RequestAccessPolicy access) {
        this.requestRepository = requestRepository;
        this.fundRepository = fundRepository;
        this.eventPublisher = eventPublisher;
        this.access = access;
    }

    @Override
    public RequestResponse bookRequest(Long requestId, Long fundId) {
        var viewer = access.currentViewer();
        viewer.requireBookingFund(fundId);
        Request request = requestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));

        if (fund.getStatus() != FundStatus.APPROVED) {
            throw new FundNotApprovedException(fundId);
        }
        if (!viewer.isAdmin()) viewer.requireBook(request);
        request.book(fund);

        eventPublisher.publishEvent(new RequestBookedEvent(requestId, fundId));

        return RequestMapper.toResponse(request);
    }
}