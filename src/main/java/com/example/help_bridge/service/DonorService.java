package com.example.help_bridge.service;

import com.example.help_bridge.dto.request.DonorRequest;
import com.example.help_bridge.dto.response.DonorResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonorService {

    public DonorResponse addNewDonor(DonorRequest request) {
        return null;
    }

    public DonorResponse getDonorById(Long donorId) {
        return null;
    }

    public List<DonorResponse> getAllDonors(String sort, Long page, Long size) {
        return null;
    }

    public DonorResponse updateDonorFields(Long donorId, DonorRequest request) {
        return null;
    }

    public DonorResponse deleteDonorById(Long donorId) {
        return null;
    }
}
