package com.example.help_bridge.fundraising.fundraiser.service.impl;

import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.EvidenceNotFoundException;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.fundraising.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.response.EvidenceResponse;
import com.example.help_bridge.fundraising.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.repository.EvidenceRepository;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraising.fundraiser.service.EvidenceService;
import com.example.help_bridge.fundraising.fundraiser.strategy.EvidenceValidatorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EvidenceServiceImpl implements EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final FundraiserRepository fundraiserRepository;
    private final List<EvidenceValidatorStrategy> strategies;

    @Override
    public EvidenceResponse addEvidenceToFundraiser(Long fundraiserId, AddEvidenceRequest request) {
        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId)
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser with ID " + fundraiserId + " not found"));

        Evidence evidence = new Evidence();
        evidence.setFundraiser(fundraiser);
        evidence.setReceiptNumber(request.receiptNumber());
        evidence.setAttachmentUrl(request.attachmentUrl());
        evidence.setRecipientFeedback(request.recipientFeedback());
        evidence.setCreatedAt(LocalDateTime.now());

        boolean isValid = false;
        for (EvidenceValidatorStrategy strategy : strategies) {
            if (strategy.supports(evidence)) {
                strategy.validate(evidence);
                isValid = true;
            }
        }

        if (!isValid) {
            throw new InvalidEvidenceException("Provided evidence does not match any known format");
        }

        Evidence saved = evidenceRepository.save(evidence);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceResponse> getEvidencesByFundraiserId(Long fundraiserId) {
        return evidenceRepository.findAllByFundraiserIdOrderByCreatedAtAsc(fundraiserId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String deleteEvidenceById(Long evidenceId) {
        if (!evidenceRepository.existsById(evidenceId)) {
            throw new EvidenceNotFoundException(evidenceId);
        }
        evidenceRepository.deleteById(evidenceId);
        return "Deleted successfully";
    }

    private EvidenceResponse mapToResponse(Evidence e) {
        return new EvidenceResponse(
                e.getId(),
                e.getFundraiser().getId(),
                e.getReceiptNumber(),
                e.getRecipientFeedback(),
                e.getAttachmentUrl(),
                e.getCreatedAt()
        );
    }
}