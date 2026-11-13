package com.example.help_bridge.fundraising.fundraiser.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Set;

import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidFundraiserStateException;
import com.example.help_bridge.fundraising.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.response.EvidenceResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserResponse;
import com.example.help_bridge.fundraising.fundraiser.dto.response.SendMailingResponse;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.event.MassMailingRequestedEvent;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraising.fundraiser.service.FundraiserService;



import com.example.help_bridge.fundraising.fundraiser.security.FundraiserAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FundraiserServiceImpl implements FundraiserService {

    private final FundraiserRepository FundraiserRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FundraiserAccessPolicy access;
    

    @Override
    public FundraiserResponse getFundraiserById(Long id) {
        Fundraiser fundraiser = FundraiserRepository.findById(id)
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser with ID " + id + " not found"));

        access.requireAccess(fundraiser);
        return mapToResponse(fundraiser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findFundraiserIdByRequestId(Long requestId) {
        access.currentViewer();
        return FundraiserRepository.findFirstByRequestIdOrderByIdAsc(requestId).map(fundraiser -> {
            access.requireAccess(fundraiser);
            return fundraiser.getId();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FundraiserResponse> getFundraisers(
            String sortBy, String sort, Long page, Long size) {
        var viewer = access.currentViewer();
        if (page == null || page < 1 || page > Integer.MAX_VALUE) {
            throw badParameter("page must be between 1 and " + Integer.MAX_VALUE);
        }
        if (size == null || size < 1 || size > 100) {
            throw badParameter("size must be between 1 and 100");
        }
        if ((page - 1) * size > Integer.MAX_VALUE) {
            throw badParameter("Requested page offset is too large");
        }
        if (!Set.of("createdAt", "status", "closedAt").contains(sortBy == null ? "" : sortBy)) {
            throw badParameter("sortBy must be createdAt, status or closedAt");
        }
        if (!"asc".equalsIgnoreCase(sort) && !"desc".equalsIgnoreCase(sort)) {
            throw badParameter("sort must be asc or desc");
        }
        var direction = Sort.Direction.fromString(sort);
        var ordering = Sort.by(direction, sortBy).and(
                Sort.by(direction, "id"));
        var pageable = PageRequest.of(page.intValue() - 1, size.intValue(), ordering);
        var fundraisers = viewer.isAdmin() ? FundraiserRepository.findAll(pageable)
                : FundraiserRepository.findAllByFundId(viewer.fundId(), pageable);
        return fundraisers.map(this::mapToResponse);
    }

    private static ResponseStatusException badParameter(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @Override
    public SendMailingResponse sendMailToDonors(SendMailingRequest request) {
        var fundraiser = FundraiserRepository.findById(request.fundraiserId())
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser not found"));
        access.requireAccess(fundraiser);
        eventPublisher.publishEvent(new MassMailingRequestedEvent(
                request.fundraiserId(),
                request.subjectOfMail(),
                request.message()
        ));
        
        return new SendMailingResponse(request.fundraiserId(), 0, LocalDateTime.now());
    }

    public void closeFundraiser(Long id) {
        Fundraiser fundraiser = FundraiserRepository.findById(id)
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser with ID " + id + " not found"));

        access.requireAccess(fundraiser);

        if (fundraiser.getStatus() == FundraiserStatus.CLOSED) {
            throw new InvalidFundraiserStateException("This fundraiser is already closed!");
        }

        if (fundraiser.getEvidences() == null || fundraiser.getEvidences().isEmpty()) {
            throw new InvalidFundraiserStateException("Cannot close a fundraiser without attached evidence!");
        }

        fundraiser.setStatus(FundraiserStatus.CLOSED);
        fundraiser.setClosedAt(LocalDateTime.now());
        FundraiserRepository.save(fundraiser);
    }

    private FundraiserResponse mapToResponse(Fundraiser fundraiser) {
        List<EvidenceResponse> evidenceResponses = fundraiser.getEvidences() == null ? List.of() :
                fundraiser.getEvidences().stream()
                        .map(e -> new EvidenceResponse(
                                e.getId(),
                                fundraiser.getId(),
                                e.getReceiptNumber(),
                                e.getRecipientFeedback(),
                                e.getAttachmentUrl(),
                                e.getCreatedAt()
                        ))
                        .collect(Collectors.toList());

        return new FundraiserResponse(
                fundraiser.getId(),
                fundraiser.getRequestId(),
                fundraiser.getStatus(),
                fundraiser.getCreatedAt(),
                fundraiser.getClosedAt(),
                evidenceResponses
        );
    }
}