package com.example.help_bridge.users.fundrepresentative.controller;

import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeRequest;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeUpdateRequest;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeResponse;
import com.example.help_bridge.users.fundrepresentative.service.FundRepresentativeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fund-representatives")
public class FundRepresentativeController {

    private final FundRepresentativeService service;

    public FundRepresentativeController(FundRepresentativeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FundRepresentativeResponse create(@Valid @RequestBody FundRepresentativeRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public FundRepresentativeResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<FundRepresentativeResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/fund/{fundId}")
    public List<FundRepresentativeResponse> getByFund(@PathVariable Long fundId) {
        return service.getByFundId(fundId);
    }

    @PutMapping("/{id}")
    public FundRepresentativeResponse update(@PathVariable Long id,
                                             @Valid @RequestBody FundRepresentativeUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}