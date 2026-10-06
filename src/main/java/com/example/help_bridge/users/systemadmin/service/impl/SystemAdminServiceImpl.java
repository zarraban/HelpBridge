package com.example.help_bridge.users.systemadmin.service.impl;

import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminUpdateRequest;
import com.example.help_bridge.users.systemadmin.dto.response.SystemAdminResponse;
import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.exception.DuplicateSystemAdminException;
import com.example.help_bridge.users.systemadmin.exception.SystemAdminHasVerificationActsException;
import com.example.help_bridge.users.systemadmin.exception.SystemAdminNotFoundException;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.systemadmin.service.SystemAdminService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemAdminServiceImpl implements SystemAdminService {

    private final SystemAdminRepository systemAdminRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SystemAdminServiceImpl(SystemAdminRepository systemAdminRepository) {
        this.systemAdminRepository = systemAdminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemAdminResponse> getAllSystemAdmins() {
        return systemAdminRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAdminResponse getSystemAdminById(Long id) {
        return mapToDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public SystemAdminResponse createSystemAdmin(SystemAdminCreateRequest request) {
        if (systemAdminRepository.existsByEmail(request.email())) {
            throw new DuplicateSystemAdminException(request.email());
        }
        SystemAdmin admin = new SystemAdmin(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName()
        );
        try {
            return mapToDto(systemAdminRepository.saveAndFlush(admin));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSystemAdminException(request.email(), e);
        }
    }

    @Override
    @Transactional
    public SystemAdminResponse updateSystemAdmin(Long id, SystemAdminUpdateRequest request) {
        SystemAdmin admin = getOrThrow(id);
        admin.setFirstName(request.firstName());
        admin.setLastName(request.lastName());
        return mapToDto(admin);
    }

    @Override
    @Transactional
    public void deleteSystemAdminById(Long id) {
        SystemAdmin admin = getOrThrow(id);
        try {
            systemAdminRepository.delete(admin);
            systemAdminRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new SystemAdminHasVerificationActsException(id, e);
        }
    }

    private SystemAdmin getOrThrow(Long id) {
        return systemAdminRepository.findById(id)
                .orElseThrow(() -> new SystemAdminNotFoundException(id));
    }

    private SystemAdminResponse mapToDto(SystemAdmin admin) {
        return new SystemAdminResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getFirstName(),
                admin.getLastName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemAdminResponse> searchByLastName(String lastName) {
        return systemAdminRepository.searchByLastName(lastName).stream()
                .map(this::mapToDto)
                .toList();
    }
}