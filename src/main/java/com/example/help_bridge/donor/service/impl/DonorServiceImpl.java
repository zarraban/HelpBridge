package com.example.help_bridge.donor.service.impl;

import com.example.help_bridge.donor.dto.request.DonorRequest;
import com.example.help_bridge.donor.dto.response.DonorResponse;
import com.example.help_bridge.donor.entity.Donor;
import com.example.help_bridge.donor.repository.DonorRepository;
import com.example.help_bridge.donor.service.DonorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.help_bridge.common.exception.DonorNotFoundException;

@Service
@RequiredArgsConstructor
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;

    @Override
    public DonorResponse addNewDonor(DonorRequest request) {
        Donor saved = donorRepository.save(mapToEntity(request));
        return mapToResponse(saved);
    }

    @Override
    public DonorResponse getDonorById(Long donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new DonorNotFoundException("Donor with the specified ID was not found"));
        return mapToResponse(donor);
    }

    @Override
    public List<DonorResponse> getAllDonors(String sort, Long page, Long size) {
        return donorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonorResponse> getDonorsByFundraiserId(Long fundraiserId) {
        return donorRepository.findByFundraiserId(fundraiserId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DonorResponse updateDonorFields(Long donorId, DonorRequest request) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new DonorNotFoundException("Donor with the specified ID was not found"));
        donor.setFundraiserId(request.fundraiserId());
        donor.setFirstName(request.firstName());
        donor.setLastName(request.lastName());
        donor.setEmail(request.email());
        donor.setPhone(request.phone());
        return mapToResponse(donorRepository.save(donor));
    }

    @Override
    public DonorResponse deleteDonorById(Long donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new DonorNotFoundException("Donor with the specified ID was not found"));
        donorRepository.deleteById(donorId);
        return mapToResponse(donor);
    }

    private DonorResponse mapToResponse(Donor donor) {
        return new DonorResponse(
                donor.getId(),
                donor.getFundraiserId(),
                donor.getFirstName(),
                donor.getLastName(),
                donor.getEmail(),
                donor.getPhone(),
                donor.getCreatedAt()
        );
    }

    private Donor mapToEntity(DonorRequest donorRequest){
        Donor donor = new Donor();
        donor.setId(donor.getId());
        donor.setFirstName(donorRequest.firstName());
        donor.setLastName(donorRequest.lastName());
        donor.setEmail(donorRequest.email());
        donor.setPhone(donorRequest.phone());
        donor.setFundraiserId(donorRequest.fundraiserId());
        donor.setCreatedAt(LocalDateTime.now());
        return donor;
    }
}