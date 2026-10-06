package com.example.help_bridge.fundraising.request.service;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.CreateRequestRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.dto.request.RequestDto.UpdateRequestRequest;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequesterNotFoundException;
import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public RequestServiceImpl(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RequestResponse createRequest(CreateRequestRequest dto) {
        User requester = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RequesterNotFoundException(dto.userId()));

        Request request = new Request(
                requester,
                dto.assistanceType(),
                dto.amount(),
                dto.deadline(),
                dto.situationDescription(),
                dto.needDescription(),
                dto.institutionName(),
                dto.applicationNumber(),
                dto.dataProcessingConsent()
        );

        return RequestMapper.toResponse(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponse> getAllRequests(RequestStatus status) {
        List<Request> requests = (status == null)
                ? requestRepository.findAllWithDetails()
                : requestRepository.findAllByStatusWithDetails(status);
        return requests.stream().map(RequestMapper::toResponse).toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<RequestResponse> getSharedPool() {
        return requestRepository.findAllByStatusWithDetails(RequestStatus.NEW).stream()
                .sorted(Comparator
                        .comparing((Request r) -> !r.isExpired())
                        .thenComparing(Request::getDeadline))
                .map(RequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponse> searchByInstitution(String institution) {
        return requestRepository.searchByInstitutionWithDetails(institution.trim()).stream()
                .map(RequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponse> getRequestsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RequesterNotFoundException(userId);
        }
        return requestRepository.findByRequesterIdWithDetails(userId).stream()
                .map(RequestMapper::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public RequestResponse getRequestById(Long id) {
        Request request = requestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
        return RequestMapper.toResponse(request);
    }

    @Override
    public RequestResponse updateRequest(Long id, UpdateRequestRequest dto) {
        Request request = requestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RequestNotFoundException(id));

        request.updateDetails(
                dto.assistanceType(),
                dto.amount(),
                dto.deadline(),
                dto.situationDescription(),
                dto.needDescription(),
                dto.institutionName(),
                dto.applicationNumber()
        );

        return RequestMapper.toResponse(requestRepository.save(request));
    }

    @Override
    public void deleteRequest(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
        request.ensureDeletable();
        requestRepository.delete(request);
    }
}