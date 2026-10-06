package com.example.help_bridge.fundraising.fund.service.impl;

import com.example.help_bridge.fundraising.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundDescriptUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.response.FundResponse;
import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.fund.event.FundStatusChangedEvent;
import com.example.help_bridge.fundraising.fund.exception.DuplicateFundException;
import com.example.help_bridge.fundraising.fund.exception.FundHasRequestsException;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.fund.exception.InvalidFundStatusTransitionException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.fund.service.FundService;
import com.example.help_bridge.fundraising.fund.strategy.FundStatusTransitionHandler;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeFundResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.exception.FundRepresentativeNotFoundException;
import com.example.help_bridge.fundraising.fund.dto.request.FundUpdateRequest;
import java.util.HashMap;
import java.util.List;

@Service
public class FundServiceImpl implements FundService {

    private final FundRepository fundRepository;
    private final List<FundStatusTransitionHandler> transitionHandlers;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestRepository requestRepository;

    public FundServiceImpl(FundRepository fundRepository,
                           List<FundStatusTransitionHandler> transitionHandlers,
                           ApplicationEventPublisher eventPublisher,
                           RequestRepository requestRepository) {
        this.fundRepository = fundRepository;
        this.transitionHandlers = transitionHandlers;
        this.eventPublisher = eventPublisher;
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundResponse> getAllFunds() {
        return fundRepository.findAllWithRepresentatives().stream()
                .map(this::mapFundToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FundResponse getFundById(Long id) {
        return mapFundToDto(getFundOrThrow(id));
    }

    @Override
    @Transactional
    public FundResponse createFund(FundCreateRequest request) {
        if (fundRepository.existsByEdrpou(request.edrpou())) {
            throw new DuplicateFundException(request.edrpou());
        }
        Fund saved = fundRepository.save(mapRequestDtoToFund(request));
        return mapFundToDto(saved);
    }

    @Override
    @Transactional
    public FundResponse updateFundStatus(Long id, FundStatusUpdateRequest request) {
        Fund fund = getFundOrThrow(id);
        FundStatus currentStatus = fund.getStatus();
        FundStatus targetStatus = request.status();

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidFundStatusTransitionException(currentStatus, targetStatus);
        }

        fund.setStatus(targetStatus);

        transitionHandlers.stream()
                .filter(handler -> handler.supports(targetStatus))
                .forEach(handler -> handler.handle(fund));

        eventPublisher.publishEvent(new FundStatusChangedEvent(fund.getId(), currentStatus, targetStatus));

        return mapFundToDto(fund);
    }

    @Override
    @Transactional
    public FundResponse updateFundDescription(Long id, FundDescriptUpdateRequest request) {
        Fund fund = getFundOrThrow(id);
        fund.setDescription(request.description());
        return mapFundToDto(fund);
    }

    @Override
    @Transactional
    public FundResponse updateFund(Long id, FundUpdateRequest request) {
        Fund fund = getFundOrThrow(id);
        fund.setFundName(request.fundName());
        fund.setBankDetail(request.bankDetail());
        fund.setRegisteredAddress(request.registeredAddress());
        fund.setActualAddress(request.actualAddress());
        fund.setPhoneNumber(request.phoneNumber());
        fund.setCorpEmail(request.corpEmail());
        fund.setWebsite(request.website());
        fund.getSocialMediaUrls().clear();
        if (request.socialMediaUrls() != null) {
            fund.getSocialMediaUrls().putAll(request.socialMediaUrls());
        }
        return mapFundToDto(fund);
    }

    @Override
    @Transactional
    public void deleteFundById(Long id) {
        Fund fund = getFundOrThrow(id);
        if (requestRepository.existsByFundId(id)) {
            throw new FundHasRequestsException(id);
        }
        fundRepository.delete(fund);
    }

    private Fund getFundOrThrow(Long id) {
        return fundRepository.findByIdWithRepresentatives(id)
                .orElseThrow(() -> new FundNotFoundException(id));
    }

    private FundResponse mapFundToDto(Fund fund) {
        return new FundResponse(
                fund.getId(),
                fund.getFundName(),
                fund.getEdrpou(),
                fund.getPhoneNumber(),
                fund.getBankDetail(),
                fund.getRegisteredAddress(),
                fund.getActualAddress(),
                fund.getCorpEmail(),
                fund.getWebsite(),
                new HashMap<>(fund.getSocialMediaUrls()),
                fund.getDescription(),
                fund.getStatus(),
                fund.getRepresentatives().stream()
                        .map(FundRepresentativeFundResponse::from)
                        .toList()
        );
    }


    private Fund mapRequestDtoToFund(FundCreateRequest request) {
        return new Fund(
                request.fundName(),
                request.edrpou(),
                request.bankDetail(),
                request.registeredAddress(),
                request.actualAddress(),
                request.phoneNumber(),
                request.corpEmail(),
                request.website(),
                request.socialMediaUrls()
        );
    }

    @Override
    @Transactional
    public void removeRepresentative(Long fundId, Long representativeId) {
        Fund fund = fundRepository.findByIdWithRepresentatives(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));

        FundRepresentative representative = fund.getRepresentatives().stream()
                .filter(r -> representativeId.equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new FundRepresentativeNotFoundException(representativeId));

        fund.removeRepresentative(representative);
    }
}