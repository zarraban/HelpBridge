package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestVerificationServiceImpl implements RequestVerificationService {

    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public void reviewRequest(Long id, RequestVerificationDto reviewRequest) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));

        if (Boolean.TRUE.equals(reviewRequest.approved())) {
            request.transitionTo(RequestStatus.NEW);
        } else {
            requestRepository.delete(request);
        }
    }
}