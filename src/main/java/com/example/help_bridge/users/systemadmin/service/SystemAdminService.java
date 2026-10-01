package com.example.help_bridge.users.systemadmin.service;

import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminUpdateRequest;
import com.example.help_bridge.users.systemadmin.dto.response.SystemAdminResponse;

import java.util.List;

public interface SystemAdminService {
    List<SystemAdminResponse> getAllSystemAdmins();
    SystemAdminResponse getSystemAdminById(Long id);
    SystemAdminResponse createSystemAdmin(SystemAdminCreateRequest request);
    SystemAdminResponse updateSystemAdmin(Long id, SystemAdminUpdateRequest request);
    void deleteSystemAdminById(Long id);
}