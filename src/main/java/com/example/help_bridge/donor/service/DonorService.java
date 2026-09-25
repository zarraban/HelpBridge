package com.example.help_bridge.donor.service;

import com.example.help_bridge.donor.dto.request.DonorRequest;
import com.example.help_bridge.donor.dto.response.DonorResponse;
import java.util.List;

public interface DonorService {
    DonorResponse addNewDonor(DonorRequest request);
    DonorResponse getDonorById(Long donorId);
    List<DonorResponse> getAllDonors(String sort, Long page, Long size);
    List<DonorResponse> getDonorsByFundraiserId(Long fundraiserId);
    DonorResponse updateDonorFields(Long donorId, DonorRequest request);
    DonorResponse deleteDonorById(Long donorId);
}
