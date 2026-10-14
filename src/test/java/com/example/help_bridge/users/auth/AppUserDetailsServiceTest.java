package com.example.help_bridge.users.auth;

import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private FundRepresentativeRepository fundRepresentativeRepository;

    @Mock
    private SystemAdminRepository systemAdminRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private UserRepository userRepository;

    private AppUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new AppUserDetailsService(
                fundRepresentativeRepository,
                systemAdminRepository,
                volunteerRepository,
                userRepository
        );
    }

    private Volunteer volunteer(VolunteerStatus status, String passwordHash) {
        Volunteer volunteer = new Volunteer();
        volunteer.setEmail("vera@vol.test");
        volunteer.setPasswordHash(passwordHash);
        volunteer.setStatus(status);
        return volunteer;
    }

    @Test
    void loadsActiveVolunteerAsEnabledUserWithVolunteerRole() {
        when(volunteerRepository.findByEmail("vera@vol.test"))
                .thenReturn(Optional.of(volunteer(VolunteerStatus.ACTIVE, "hash")));

        UserDetails user = service.loadUserByUsername("VOLUNTEER:vera@vol.test");

        assertThat(user.getUsername()).isEqualTo("VOLUNTEER:vera@vol.test");
        assertThat(user.getPassword()).isEqualTo("hash");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_VOLUNTEER");
    }

    @Test
    void loadsInactiveVolunteerAsDisabledUser() {
        when(volunteerRepository.findByEmail("vera@vol.test"))
                .thenReturn(Optional.of(volunteer(VolunteerStatus.INACTIVE, "hash")));

        UserDetails user = service.loadUserByUsername("VOLUNTEER:vera@vol.test");

        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void looksVolunteerUpByLowerCasedEmailAndAcceptsLowerCaseType() {
        when(volunteerRepository.findByEmail("vera@vol.test"))
                .thenReturn(Optional.of(volunteer(VolunteerStatus.ACTIVE, "hash")));

        UserDetails user = service.loadUserByUsername("volunteer:Vera@Vol.TEST");

        assertThat(user.getUsername()).isEqualTo("VOLUNTEER:Vera@Vol.TEST");
        verify(volunteerRepository).findByEmail("vera@vol.test");
    }

    @Test
    void treatsVolunteerWithoutPasswordAsNotFound() {
        when(volunteerRepository.findByEmail("vera@vol.test"))
                .thenReturn(Optional.of(volunteer(VolunteerStatus.ACTIVE, null)));

        assertThatThrownBy(() -> service.loadUserByUsername("VOLUNTEER:vera@vol.test"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void throwsNotFoundForUnknownVolunteer() {
        when(volunteerRepository.findByEmail("nobody@vol.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("VOLUNTEER:nobody@vol.test"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadsSystemAdminWithAdminRole() {
        SystemAdmin admin = new SystemAdmin("admin@helpbridge.test", "adminHash", "Ada", "Admin");
        when(systemAdminRepository.findByEmail("admin@helpbridge.test")).thenReturn(Optional.of(admin));

        UserDetails user = service.loadUserByUsername("SYSTEM_ADMIN:admin@helpbridge.test");

        assertThat(user.getPassword()).isEqualTo("adminHash");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_SYSTEM_ADMIN");
    }

    @Test
    void loadsApplicantWithApplicantRole() {
        User applicant = new User("Оксана", "Афанасьєва", "applicant@helpbridge.test", "applicantHash");
        when(userRepository.findByEmail("applicant@helpbridge.test")).thenReturn(Optional.of(applicant));

        UserDetails user = service.loadUserByUsername("APPLICANT:applicant@helpbridge.test");

        assertThat(user.getUsername()).isEqualTo("APPLICANT:applicant@helpbridge.test");
        assertThat(user.getPassword()).isEqualTo("applicantHash");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_APPLICANT");
    }

    @Test
    void doesNotLookEmailUpInOtherAccountTypes() {
        when(volunteerRepository.findByEmail("admin@helpbridge.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("VOLUNTEER:admin@helpbridge.test"))
                .isInstanceOf(UsernameNotFoundException.class);
        verifyNoInteractions(systemAdminRepository, fundRepresentativeRepository, userRepository);
    }

    @Test
    void throwsNotFoundWhenUsernameHasNoAccountType() {
        assertThatThrownBy(() -> service.loadUserByUsername("vera@vol.test"))
                .isInstanceOf(UsernameNotFoundException.class);
        verifyNoInteractions(volunteerRepository, systemAdminRepository, fundRepresentativeRepository, userRepository);
    }

    @Test
    void throwsNotFoundForUnknownAccountType() {
        assertThatThrownBy(() -> service.loadUserByUsername("WIZARD:vera@vol.test"))
                .isInstanceOf(UsernameNotFoundException.class);
        verifyNoInteractions(volunteerRepository, systemAdminRepository, fundRepresentativeRepository, userRepository);
    }
}