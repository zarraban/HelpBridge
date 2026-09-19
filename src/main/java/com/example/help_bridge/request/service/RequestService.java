package com.example.help_bridge.request.service;

import com.example.help_bridge.request.dto.RequestDto.CreateRequestRequest;
import com.example.help_bridge.request.dto.RequestDto.RequestResponse;
import com.example.help_bridge.request.entity.RequestStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final Map<Long, RequestEntity> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public RequestResponse createRequest(CreateRequestRequest requestDto) {
        Long id = idGenerator.getAndIncrement();
        RequestEntity entity = new RequestEntity(
                id,
                requestDto.assistanceType(),
                requestDto.amount(),
                requestDto.deadline(),
                requestDto.situationDescription(),
                requestDto.needDescription(),
                requestDto.institutionName(),
                requestDto.applicationNumber(),
                RequestStatus.PENDING_VERIFICATION
        );
        storage.put(id, entity);
        return mapToResponse(entity);
    }

    public RequestEntity getById(Long id) {
        RequestEntity entity = storage.get(id);
        if (entity == null) {
            throw new NoSuchElementException("Request with ID " + id + " was not found");
        }
        return entity;
    }

    public void updateStatus(Long id, RequestStatus newStatus) {
        RequestEntity entity = getById(id);
        RequestEntity updated = new RequestEntity(
                entity.id(), entity.assistanceType(), entity.amount(), entity.deadline(),
                entity.situationDescription(), entity.needDescription(), entity.institutionName(),
                entity.applicationNumber(), newStatus
        );
        storage.put(id, updated);
    }

    // ОСЬ ЦЕЙ МЕТОД ПОТРІБНО ДОДАТИ (або оновити клас повністю)
    public void deleteRequest(Long id) {
        if (!storage.containsKey(id)) {
            throw new NoSuchElementException("Request with ID " + id + " was not found");
        }
        storage.remove(id);
    }

    public List<RequestResponse> getSharedPool() {
        LocalDate now = LocalDate.now();
        return storage.values().stream()
                .filter(r -> r.status() == RequestStatus.NEW)
                .sorted((r1, r2) -> {
                    boolean r1Expired = r1.deadline().isBefore(now);
                    boolean r2Expired = r2.deadline().isBefore(now);
                    if (r1Expired && !r2Expired) return -1;
                    if (!r1Expired && r2Expired) return 1;
                    return r1.deadline().compareTo(r2.deadline());
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RequestResponse mapToResponse(RequestEntity entity) {
        boolean isHot = entity.deadline().isBefore(LocalDate.now()) && entity.status() != RequestStatus.CLOSED;
        return new RequestResponse(
                entity.id(), entity.assistanceType(), entity.amount(), entity.deadline(),
                entity.situationDescription(), entity.needDescription(), entity.institutionName(),
                entity.applicationNumber(), entity.status(), isHot
        );
    }

    public record RequestEntity(
            Long id, String assistanceType, java.math.BigDecimal amount, LocalDate deadline,
            String situationDescription, String needDescription, String institutionName,
            String applicationNumber, RequestStatus status
    ) {}
}