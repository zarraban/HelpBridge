package com.example.help_bridge.donor.controller;

import com.example.help_bridge.donor.dto.request.DonorRequest;
import com.example.help_bridge.donor.dto.response.DonorResponse;
import com.example.help_bridge.donor.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO When using Thymeleaf will be converted to @Controller
@RestController
@RequestMapping("/api/donors")
@Validated
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService){
        this.donorService = donorService;
    }

    @PostMapping
    public ResponseEntity<DonorResponse> addNewDonor(
            @RequestBody @Valid DonorRequest request
    ) {
        return ResponseEntity.ok(donorService.addNewDonor(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponse> getDonorById(
            @PathVariable("id") Long donorId
    ) {
        return ResponseEntity.ok(donorService.getDonorById(donorId));
    }

    @GetMapping
    public ResponseEntity<List<DonorResponse>> getAllDonors(
            @RequestParam(value = "sort", defaultValue = "asc") String sort,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "5") Long size
    ) {
        return ResponseEntity.ok(donorService.getAllDonors(sort, page, size));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DonorResponse> updateDonorFields(
            @PathVariable("id") Long donorId,
            @RequestBody @Valid DonorRequest request
    ) {
        return ResponseEntity.ok(donorService.updateDonorFields(donorId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DonorResponse> deleteDonorById(
            @PathVariable("id") Long donorId
    ) {
        // 404 - if there is no such entity to delete
        // 204 - if we have deleted successfully the record
        return ResponseEntity.ok(donorService.deleteDonorById(donorId));
    }

}
