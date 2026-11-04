package com.example.help_bridge.common.config;

import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdAdminSeederTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SystemAdminRepository systemAdminRepository;

    @InjectMocks
    private ProdAdminSeeder prodAdminSeeder;

    @Test
    @DisplayName("Should create SystemAdmin when email and password are provided and admin does not exist")
    void shouldCreateAdminWhenNotExists() {
        ReflectionTestUtils.setField(prodAdminSeeder, "adminEmail", "prod-admin@helpbridge.ua");
        ReflectionTestUtils.setField(prodAdminSeeder, "adminPassword", "SecurePass2026!");

        when(systemAdminRepository.existsByEmail("prod-admin@helpbridge.ua")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass2026!")).thenReturn("encodedHash123");

        prodAdminSeeder.run(new DefaultApplicationArguments());

        ArgumentCaptor<SystemAdmin> captor = ArgumentCaptor.forClass(SystemAdmin.class);
        verify(systemAdminRepository).save(captor.capture());

        SystemAdmin savedAdmin = captor.getValue();
        assertThat(savedAdmin.getEmail()).isEqualTo("prod-admin@helpbridge.ua");
        assertThat(savedAdmin.getPasswordHash()).isEqualTo("encodedHash123");
    }

    @Test
    @DisplayName("Should not create admin if admin with this email already exists")
    void shouldNotCreateAdminWhenAlreadyExists() {
        ReflectionTestUtils.setField(prodAdminSeeder, "adminEmail", "prod-admin@helpbridge.ua");
        ReflectionTestUtils.setField(prodAdminSeeder, "adminPassword", "SecurePass2026!");

        when(systemAdminRepository.existsByEmail("prod-admin@helpbridge.ua")).thenReturn(true);

        prodAdminSeeder.run(new DefaultApplicationArguments());

        verify(systemAdminRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should skip creation when variables are blank")
    void shouldSkipWhenVariablesAreBlank() {
        ReflectionTestUtils.setField(prodAdminSeeder, "adminEmail", "");
        ReflectionTestUtils.setField(prodAdminSeeder, "adminPassword", "");

        prodAdminSeeder.run(new DefaultApplicationArguments());

        verify(systemAdminRepository, never()).existsByEmail(anyString());
        verify(systemAdminRepository, never()).save(any());
    }
}
