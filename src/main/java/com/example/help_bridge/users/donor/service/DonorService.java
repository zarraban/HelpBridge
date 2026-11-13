package com.example.help_bridge.users.donor.service;

import com.example.help_bridge.users.donor.dto.request.DonorRequest;
import com.example.help_bridge.users.donor.dto.response.DonorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DonorService {
    DonorResponse addNewDonor(DonorRequest request);
    DonorResponse getDonorById(Long donorId);
    List<DonorResponse> getAllDonors(String sort, Long page, Long size);
    Page<DonorResponse> getDonorPage(Pageable pageable);
    List<DonorResponse> getDonorsByFundraiserId(Long fundraiserId);
    DonorResponse updateDonorFields(Long donorId, DonorRequest request);
    DonorResponse deleteDonorById(Long donorId);
}
