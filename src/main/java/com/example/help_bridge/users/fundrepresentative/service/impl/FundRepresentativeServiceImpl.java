package com.example.help_bridge.users.fundrepresentative.service.impl;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeRequest;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeUpdateRequest;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeResponse;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.exception.DuplicateFundRepresentativeException;
import com.example.help_bridge.users.fundrepresentative.exception.FundRepresentativeNotFoundException;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.fundrepresentative.service.FundRepresentativeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FundRepresentativeServiceImpl implements FundRepresentativeService {

    private final FundRepresentativeRepository repository;
    private final FundRepository fundRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public FundRepresentativeServiceImpl(FundRepresentativeRepository repository, FundRepository fundRepository) {
        this.repository = repository;
        this.fundRepository = fundRepository;
    }

    @Override
    @Transactional
    public FundRepresentativeResponse create(FundRepresentativeRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateFundRepresentativeException(request.email());
        }
        Fund fund = fundRepository.findById(request.fundId())
                .orElseThrow(() -> new FundNotFoundException(request.fundId()));

        FundRepresentative representative = new FundRepresentative(
                request.firstName(), request.lastName(), request.email(),
                request.phone(), passwordEncoder.encode((request.password())), fund);

        return FundRepresentativeResponse.from(repository.save(representative));
    }

    @Override
    public FundRepresentativeResponse getById(Long id) {
        return repository.findByIdWithFund(id)
                .map(FundRepresentativeResponse::from)
                .orElseThrow(() -> new FundRepresentativeNotFoundException(id));
    }

    @Override
    public List<FundRepresentativeResponse> getAll() {
        return repository.findAllWithFund().stream()
                .map(FundRepresentativeResponse::from)
                .toList();
    }

    @Override
    public List<FundRepresentativeResponse> getByFundId(Long fundId) {
        if (!fundRepository.existsById(fundId)) {
            throw new FundNotFoundException(fundId);
        }
        return repository.findByFundIdWithFund(fundId).stream()
                .map(FundRepresentativeResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public FundRepresentativeResponse update(Long id, FundRepresentativeUpdateRequest request) {
        FundRepresentative representative = repository.findByIdWithFund(id)
                .orElseThrow(() -> new FundRepresentativeNotFoundException(id));

        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateFundRepresentativeException(request.email());
        }

        representative.setFirstName(request.firstName());
        representative.setLastName(request.lastName());
        representative.setEmail(request.email());
        representative.setPhone(request.phone());
        return FundRepresentativeResponse.from(representative);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new FundRepresentativeNotFoundException(id);
        }
        repository.deleteById(id);
    }
}