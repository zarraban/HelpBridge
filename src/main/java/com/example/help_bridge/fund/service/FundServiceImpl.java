package com.example.help_bridge.fund.service;

import com.example.help_bridge.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fund.dto.request.FundDescriptUpdateRequest;
import com.example.help_bridge.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fund.dto.response.FundResponse;
import com.example.help_bridge.fund.entity.Fund;
import com.example.help_bridge.fund.entity.FundStatus;
import com.example.help_bridge.fund.event.FundStatusChangedEvent;
import com.example.help_bridge.fund.exception.FundNotFoundException;
import com.example.help_bridge.fund.exception.InvalidFundStatusTransitionException;
import com.example.help_bridge.fund.repository.FundRepository;
import com.example.help_bridge.fund.strategy.FundStatusTransitionHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class FundServiceImpl implements FundService {

    private final FundRepository fundRepository;
    private final List<FundStatusTransitionHandler> transitionHandlers;
    private final ApplicationEventPublisher eventPublisher;

    public FundServiceImpl(FundRepository fundRepository,
                           List<FundStatusTransitionHandler> transitionHandlers,
                           ApplicationEventPublisher eventPublisher) {
        this.fundRepository = fundRepository;
        this.transitionHandlers = transitionHandlers;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<FundResponse> getAllFunds() {
        return fundRepository.findAll().stream()
                .map(this::mapFundToDto)
                .toList();
    }

    @Override
    public FundResponse getFundById(Long id) {
        return mapFundToDto(getFundOrThrow(id));
    }

    @Override
    public FundResponse createFund(FundCreateRequest request) {
        Fund fund = mapRequestDtoToFund(request);
        fund.setStatus(FundStatus.PENDING_APPROVAL);
        Fund saved = fundRepository.save(fund);
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
        Fund saved = fundRepository.save(fund);

        transitionHandlers.stream()
                .filter(handler -> handler.supports(targetStatus))
                .forEach(handler -> handler.handle(saved));

        eventPublisher.publishEvent(new FundStatusChangedEvent(saved.getId(), currentStatus, targetStatus));

        return mapFundToDto(saved);
    }

    @Override
    public FundResponse updateFundDescription(Long id, FundDescriptUpdateRequest request) {
        Fund fund = getFundOrThrow(id);
        fund.setDescription(request.description());
        Fund saved = fundRepository.save(fund);
        return mapFundToDto(saved);
    }

    @Override
    public void deleteFundById(Long id) {
        getFundOrThrow(id);
        fundRepository.deleteById(id);
    }

    private Fund getFundOrThrow(Long id) {
        return fundRepository.findById(id)
                .orElseThrow(() -> new FundNotFoundException(id));
    }

    private FundResponse mapFundToDto(Fund fund) {
        return new FundResponse(
                fund.getId(),
                fund.getFundRepresName(),
                fund.getFundRepresSurname(),
                fund.getFundName(),
                fund.getEdrpou(),
                fund.getPhoneNumber(),
                fund.getBankDetail(),
                fund.getRegisteredAddress(),
                fund.getActualAddress(),
                fund.getCorpEmail(),
                fund.getWebsite(),
                fund.getSocialMediaUrls(),
                fund.getDescription(),
                fund.getStatus()
        );
    }

    private Fund mapRequestDtoToFund(FundCreateRequest request) {
        Fund fund = new Fund();
        fund.setFundRepresName(request.fundRepresName());
        fund.setFundRepresSurname(request.fundRepresSurname());
        fund.setFundName(request.fundName());
        fund.setEdrpou(request.edrpou());
        fund.setBankDetail(request.bankDetail());
        fund.setRegisteredAddress(request.registeredAddress());
        fund.setActualAddress(request.actualAddress());
        fund.setPhoneNumber(request.phoneNumber());
        fund.setCorpEmail(request.corpEmail());
        fund.setWebsite(request.website());
        fund.setSocialMediaUrls(request.socialMediaUrls());
        return fund;
    }
}