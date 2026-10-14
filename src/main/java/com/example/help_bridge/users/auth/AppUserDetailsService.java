package com.example.help_bridge.users.auth;

import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final FundRepresentativeRepository fundRepresentativeRepository;
    private final SystemAdminRepository adminRepository;
    private final VolunteerRepository volunteerRepository;

    /*
    During the login/registration user will always have to specify the type of his Account.
    We are extracting account type from the first part of the username:
    ACCOUNT_TYPE:username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] typeAndEmail = username.split(":", 2);
        if (typeAndEmail.length != 2) {
            throw accountNotFound();
        }
        AccountType accountType = AccountType.fromString(typeAndEmail[0]);
        String email = typeAndEmail[1].trim();
        String principalName = accountType + ":" + email;

        return switch (accountType) {
            case FUND_REPRESENTATIVE -> fundRepresentativeRepository.findByEmailIgnoreCase(email)
                    .map(rep -> toUserDetails(principalName, rep.getPasswordHash(), accountType, true))
                    .orElseThrow(AppUserDetailsService::accountNotFound);
            case VOLUNTEER -> volunteerRepository.findByEmail(email.toLowerCase())
                    .map(volunteer -> toUserDetails(principalName, volunteer.getPasswordHash(), accountType, volunteer.isActive()))
                    .orElseThrow(AppUserDetailsService::accountNotFound);
            case SYSTEM_ADMIN -> adminRepository.findByEmail(email)
                    .map(admin -> toUserDetails(principalName, admin.getPasswordHash(), accountType, true))
                    .orElseThrow(AppUserDetailsService::accountNotFound);
        };
    }

    private static UserDetails toUserDetails(String username, String passwordHash, AccountType accountType, boolean enabled) {
        if (passwordHash == null) {
            throw accountNotFound();
        }
        return User.withUsername(username)
                .password(passwordHash)
                .roles(accountType.name())
                .disabled(!enabled)
                .build();
    }

    private static UsernameNotFoundException accountNotFound() {
        return new UsernameNotFoundException("Account was not found");
    }
}
