package com.example.help_bridge.service;

import com.example.help_bridge.dto.SharedPoolResponseDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SharedPoolService {

    private final RequestService requestService;

    public SharedPoolService(RequestService requestService) {
        this.requestService = requestService;
    }

    public List<SharedPoolResponseDto> getSharedPool() {
        LocalDate now = LocalDate.now();

        return requestService.getSharedPool().stream()
                .map(r -> new SharedPoolResponseDto(
                        r.id(),
                        r.assistanceType(),
                        r.amount(),
                        r.deadline(),
                        r.situationDescription(),
                        r.needDescription(),
                        r.institutionName(),
                        r.applicationNumber(),
                        r.deadline().isBefore(now)
                ))
                .collect(Collectors.toList());
    }
}