package com.example.help_bridge.users.donor.service.impl;

import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import com.example.help_bridge.users.donor.dto.request.DonorRequest;
import com.example.help_bridge.users.donor.dto.response.DonorResponse;
import com.example.help_bridge.users.donor.entity.Donor;
import com.example.help_bridge.users.donor.repository.DonorRepository;
import com.example.help_bridge.users.donor.service.DonorService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.help_bridge.users.donor.exception.DonorNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;
    private final EntityManager entityManager;

    @Override
    public DonorResponse addNewDonor(DonorRequest request) {
        Donor saved = donorRepository.save(mapToEntity(request));
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DonorResponse getDonorById(Long donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new DonorNotFoundException("Donor with the specified ID was not found"));
        return mapToResponse(donor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorResponse> getAllDonors(String sort, Long page, Long size) {
        return donorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorResponse> getDonorsByFundraiserId(Long fundraiserId) {
        return donorRepository.findAllByFundraiserIdWithFundraiser(fundraiserId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DonorResponse updateDonorFields(Long donorId, DonorRequest request) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new DonorNotFoundException("Donor with the specified ID was not found"));
        donor.setFundraiser(findFundraiser(request.fundraiserId()));
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

    // find, а не getReference: неіснуючий збір дає 404, а не порушення FK під час flush
    private Fundraiser findFundraiser(Long fundraiserId) {
        Fundraiser fundraiser = entityManager.find(Fundraiser.class, fundraiserId);
        if (fundraiser == null) {
            throw new FundraiserNotFoundException("Fundraiser with ID '" + fundraiserId + "' not found");
        }
        return fundraiser;
    }

    private DonorResponse mapToResponse(Donor donor) {
        return new DonorResponse(
                donor.getId(),
                donor.getFundraiser().getId(),
                donor.getFirstName(),
                donor.getLastName(),
                donor.getEmail(),
                donor.getPhone(),
                donor.getCreatedAt()
        );
    }

    private Donor mapToEntity(DonorRequest donorRequest){
        Donor donor = new Donor();
        donor.setFirstName(donorRequest.firstName());
        donor.setLastName(donorRequest.lastName());
        donor.setEmail(donorRequest.email());
        donor.setPhone(donorRequest.phone());
        donor.setFundraiser(findFundraiser(donorRequest.fundraiserId()));
        donor.setCreatedAt(LocalDateTime.now());
        return donor;
    }
}