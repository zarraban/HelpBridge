package com.example.help_bridge.controller;

import com.example.help_bridge.dto.request.DonorRequest;
import com.example.help_bridge.dto.response.DonorResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

// TODO When using Thymeleaf will be converted to @Controller
@RestController
@RequestMapping("/api/donors")
@Validated
public class DonorController {

    @PostMapping
    public ResponseEntity<DonorResponse> addNewDonor(
            @RequestBody @Valid DonorRequest request
    ) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponse> getDonorById(
            @PathVariable("id") Long donorId
    ) {
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<List<DonorResponse>> getAllDonors(
            @RequestParam(value = "sort", defaultValue = "asc") String sort,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "5") Long size
    ) {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DonorResponse> updateDonorFields(
            @PathVariable("id") Long donorId,
            @RequestBody @Valid DonorRequest request
    ) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DonorResponse> deleteDonorById(
            @PathVariable("id") Long donorId
    ) {

        // 404 - if there is no such entity to delete
        // 204 - if we have deleted successfully the record
        return ResponseEntity.ok(null);
    }

}
