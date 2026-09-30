package com.example.help_bridge.users.systemadmin.service;

import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminUpdateRequest;
import com.example.help_bridge.users.systemadmin.dto.response.SystemAdminResponse;
import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.exception.DuplicateSystemAdminException;
import com.example.help_bridge.users.systemadmin.exception.SystemAdminNotFoundException;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.systemadmin.service.impl.SystemAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemAdminServiceImplTest {

    @Mock
    private SystemAdminRepository repository;

    @InjectMocks
    private SystemAdminServiceImpl service;

    private SystemAdmin admin(Long id) {
        SystemAdmin a = new SystemAdmin("admin@help.ua", "hash", "Daria", "Chorna");
        a.setId(id);
        return a;
    }

    @Test
    void createSystemAdmin_hashesPasswordAndReturnsDto() {
        var request = new SystemAdminCreateRequest("admin@help.ua", "secret123", "Daria", "Chorna");
        when(repository.existsByEmail("admin@help.ua")).thenReturn(false);
        when(repository.saveAndFlush(any(SystemAdmin.class))).thenAnswer(inv -> {
            SystemAdmin a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        SystemAdminResponse response = service.createSystemAdmin(request);

        ArgumentCaptor<SystemAdmin> captor = ArgumentCaptor.forClass(SystemAdmin.class);
        verify(repository).saveAndFlush(captor.capture());
        SystemAdmin saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(new BCryptPasswordEncoder().matches("secret123", saved.getPasswordHash())).isTrue();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("admin@help.ua");
    }

    @Test
    void createSystemAdmin_duplicateEmail_throwsAndDoesNotSave() {
        var request = new SystemAdminCreateRequest("admin@help.ua", "secret123", "Daria", "Chorna");
        when(repository.existsByEmail("admin@help.ua")).thenReturn(true);

        assertThatThrownBy(() -> service.createSystemAdmin(request))
                .isInstanceOf(DuplicateSystemAdminException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void createSystemAdmin_constraintViolationOnFlush_translatedToDuplicate() {
        var request = new SystemAdminCreateRequest("admin@help.ua", "secret123", "Daria", "Chorna");
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.createSystemAdmin(request))
                .isInstanceOf(DuplicateSystemAdminException.class);
    }

    @Test
    void getSystemAdminById_notFound_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSystemAdminById(99L))
                .isInstanceOf(SystemAdminNotFoundException.class);
    }

    @Test
    void getAllSystemAdmins_mapsEntitiesToDtos() {
        when(repository.findAll()).thenReturn(List.of(admin(1L), admin(2L)));

        List<SystemAdminResponse> result = service.getAllSystemAdmins();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void updateSystemAdmin_changesNames() {
        SystemAdmin existing = admin(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        SystemAdminResponse response = service.updateSystemAdmin(1L, new SystemAdminUpdateRequest("Olena", "Shevchenko"));

        assertThat(response.firstName()).isEqualTo("Olena");
        assertThat(response.lastName()).isEqualTo("Shevchenko");
        assertThat(response.email()).isEqualTo("admin@help.ua");
    }

    @Test
    void deleteSystemAdminById_existing_deletes() {
        SystemAdmin existing = admin(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteSystemAdminById(1L);

        verify(repository).delete(existing);
    }

    @Test
    void deleteSystemAdminById_notFound_throwsAndDoesNotDelete() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteSystemAdminById(1L))
                .isInstanceOf(SystemAdminNotFoundException.class);
        verify(repository, never()).delete(any());
    }
}