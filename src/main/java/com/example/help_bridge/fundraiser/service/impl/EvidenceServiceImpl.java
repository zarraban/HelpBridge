package com.example.help_bridge.fundraiser.service.impl;

import com.example.help_bridge.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.fundraiser.exception.InvalidEvidenceException;
import com.example.help_bridge.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraiser.dto.response.EvidenceResponse;
import com.example.help_bridge.fundraiser.entity.Evidence;
import com.example.help_bridge.fundraiser.repository.EvidenceRepository;
import com.example.help_bridge.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraiser.service.EvidenceService;
import com.example.help_bridge.fundraiser.strategy.EvidenceValidatorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvidenceServiceImpl implements EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final FundraiserRepository fundraiserRepository;
    private final List<EvidenceValidatorStrategy> strategies;

    @Override
    public EvidenceResponse addEvidenceToFundraiser(Long fundraiserId, AddEvidenceRequest request) {
        fundraiserRepository.findById(fundraiserId)
                .orElseThrow(() -> new FundraiserNotFoundException("Fundraiser with ID " + fundraiserId + " not found"));

        Evidence evidence = new Evidence();
        evidence.setFundraiserId(fundraiserId);
        evidence.setReceiptNumber(request.receiptNumber());
        evidence.setAttachmentUrl(request.attachmentUrl());
        evidence.setRecipientFeedback(request.recipientFeedback());
        evidence.setCreatedAt(LocalDateTime.now());

        boolean isValid = false;
        for (EvidenceValidatorStrategy strategy : strategies) {
            if (strategy.supports(evidence)) {
                strategy.validate(evidence);
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new InvalidEvidenceException("Provided evidence does not match any known format");
        }

        Evidence saved = evidenceRepository.save(evidence);
        return mapToResponse(saved);
    }

    @Override
    public List<EvidenceResponse> getEvidencesByFundraiserId(Long fundraiserId) {
        return evidenceRepository.findAll().stream()
                .filter(e -> e.getFundraiserId().equals(fundraiserId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String deleteEvidenceById(Long evidenceId) {
        evidenceRepository.deleteById(evidenceId);
        return "Deleted successfully";
    }

    private EvidenceResponse mapToResponse(Evidence e) {
        return new EvidenceResponse(
                e.getId(),
                e.getFundraiserId(),
                e.getReceiptNumber(),
                e.getRecipientFeedback(),
                e.getAttachmentUrl(),
                e.getCreatedAt()
        );
    }
}