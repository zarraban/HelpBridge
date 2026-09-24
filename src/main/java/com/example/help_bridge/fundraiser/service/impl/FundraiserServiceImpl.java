package com.example.help_bridge.fundraiser.service.impl;

import com.example.help_bridge.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraiser.dto.response.EvidenceResponse;
import com.example.help_bridge.fundraiser.dto.response.FundraiserResponse;
import com.example.help_bridge.fundraiser.dto.response.SendMailingResponse;
import com.example.help_bridge.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraiser.event.MassMailingRequestedEvent;
import com.example.help_bridge.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraiser.service.FundraiserService;



import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundraiserServiceImpl implements FundraiserService {

    private final FundraiserRepository fundraiserRepository;
    private final ApplicationEventPublisher eventPublisher;
    

    @Override
    public FundraiserResponse getFundraiserById(Long id) {
        Fundraiser fundraiser = fundraiserRepository.findById(id)
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser with ID " + id + " not found"));

        return mapToResponse(fundraiser);
    }

    @Override
    public List<FundraiserResponse> getAllFundraisers(String sort, Long page, Long size) {
        return fundraiserRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SendMailingResponse sendMailToDonors(SendMailingRequest request) {
        eventPublisher.publishEvent(new MassMailingRequestedEvent(
                request.fundraiserId(),
                request.subjectOfMail(),
                request.message()
        ));
        
        return new SendMailingResponse(request.fundraiserId(), 0, LocalDateTime.now());
    }

    public void closeFundraiser(Long id) {
        Fundraiser fundraiser = fundraiserRepository.findById(id)
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser with ID " + id + " not found"));

        if (fundraiser.getStatus() == FundraiserStatus.CLOSED) {
            throw new IllegalStateException("This fundraiser is already closed!");
        }

        if (fundraiser.getEvidences() == null || fundraiser.getEvidences().isEmpty()) {
            throw new IllegalStateException("Cannot close a fundraiser without attached evidence!");
        }

        fundraiser.setStatus(FundraiserStatus.CLOSED);
        fundraiser.setClosedAt(LocalDateTime.now());
        fundraiserRepository.save(fundraiser);
    }

    private FundraiserResponse mapToResponse(Fundraiser fundraiser) {
        List<EvidenceResponse> evidenceResponses = fundraiser.getEvidences() == null ? List.of() :
                fundraiser.getEvidences().stream()
                        .map(e -> new EvidenceResponse(
                                e.getId(),
                                e.getFundraiserId(),
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