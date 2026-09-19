package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.RequestVerificationDto;
import com.example.help_bridge.request.entity.RequestStatus;
import org.springframework.stereotype.Service;

@Service
public class RequestVerificationService {

    private final RequestService requestService;

    public RequestVerificationService(RequestService requestService) {
        this.requestService = requestService;
    }

    public void reviewRequest(Long id, RequestVerificationDto reviewRequest) {
        requestService.getById(id);

        if (Boolean.TRUE.equals(reviewRequest.approved())) {
            requestService.updateStatus(id, RequestStatus.NEW);
        } else {
            requestService.deleteRequest(id); // Видаляємо повністю згідно з ТЗ
        }
    }
}