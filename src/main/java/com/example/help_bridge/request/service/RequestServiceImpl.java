package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.request.entity.Request;
import com.example.help_bridge.request.entity.RequestStatus;
import com.example.help_bridge.request.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public RequestServiceImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public RequestResponse createRequest(CreateRequestRequest dto) {
        Request request = new Request(
                idGenerator.getAndIncrement(),
                dto.assistanceType(),
                dto.amount(),
                dto.deadline(),
                dto.situationDescription(),
                dto.needDescription(),
                dto.institutionName(),
                dto.applicationNumber()
        );
        Request saved = requestRepository.save(request);
        return RequestMapper.toResponse(saved);
    }

    @Override
    public List<RequestResponse> getSharedPool() {
        return requestRepository.findAll().stream()
                .filter(r -> r.getStatus() == RequestStatus.NEW)
                .sorted(Comparator
                        .comparing((Request r) -> !r.isExpired())
                        .thenComparing(Request::getDeadline))
                .map(RequestMapper::toResponse)
                .toList();
    }
}