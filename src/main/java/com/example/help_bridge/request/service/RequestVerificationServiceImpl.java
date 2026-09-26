package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.request.entity.Request;
import com.example.help_bridge.request.entity.RequestStatus;
import com.example.help_bridge.request.exception.RequestNotFoundException;
import com.example.help_bridge.request.repository.RequestRepository;
import org.springframework.stereotype.Service;

@Service
public class RequestVerificationServiceImpl implements RequestVerificationService {

    private final RequestRepository requestRepository;

    public RequestVerificationServiceImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public void reviewRequest(Long id, RequestVerificationDto reviewRequest) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));

        if (Boolean.TRUE.equals(reviewRequest.approved())) {
            request.transitionTo(RequestStatus.NEW);
            requestRepository.save(request);
        } else {
            requestRepository.deleteById(id);
        }
    }
}