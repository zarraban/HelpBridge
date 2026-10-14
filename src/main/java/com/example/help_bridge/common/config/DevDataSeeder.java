package com.example.help_bridge.common.config;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "Pass1234!";
    public static final String ADMIN_EMAIL = "admin@helpbridge.test";
    public static final String REPRESENTATIVE_EMAIL = "representative@helpbridge.test";
    public static final String VOLUNTEER_EMAIL = "volunteer@helpbridge.test";
    public static final String APPLICANT_EMAIL = "requester@helpbridge.test";

    private static final String FUND_EDRPOU = "12345678";

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SystemAdminRepository systemAdminRepository;
    private final FundRepository fundRepository;
    private final FundRepresentativeRepository fundRepresentativeRepository;
    private final VolunteerRepository volunteerRepository;

    public DevDataSeeder(PasswordEncoder passwordEncoder,
                         UserRepository userRepository,
                         SystemAdminRepository systemAdminRepository,
                         FundRepository fundRepository,
                         FundRepresentativeRepository fundRepresentativeRepository,
                         VolunteerRepository volunteerRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.systemAdminRepository = systemAdminRepository;
        this.fundRepository = fundRepository;
        this.fundRepresentativeRepository = fundRepresentativeRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String hash = passwordEncoder.encode(DEMO_PASSWORD);

        seedApplicant(hash);
        seedAdmin(hash);
        Fund fund = seedFund();
        seedRepresentative(hash, fund);
        seedVolunteer(hash, fund);

        log.info("Example BCrypt hash of the demo password {} -> {}", DEMO_PASSWORD, hash);
        log.info("Dev accounts (log in with accountType, email and the demo password): "
                        + "{} {}, {} {}, {} {}, {} {}",
                AccountType.SYSTEM_ADMIN, ADMIN_EMAIL,
                AccountType.FUND_REPRESENTATIVE, REPRESENTATIVE_EMAIL,
                AccountType.VOLUNTEER, VOLUNTEER_EMAIL,
                AccountType.APPLICANT, APPLICANT_EMAIL);
    }

    private void seedApplicant(String hash) {
        if (userRepository.findByEmail(APPLICANT_EMAIL).isEmpty()) {
            userRepository.save(new User("Test", "Requester", APPLICANT_EMAIL, hash));
        }
    }

    private void seedAdmin(String hash) {
        if (!systemAdminRepository.existsByEmail(ADMIN_EMAIL)) {
            systemAdminRepository.save(new SystemAdmin(ADMIN_EMAIL, hash, "Test", "Admin"));
        }
    }

    private Fund seedFund() {
        return fundRepository.findByEdrpou(FUND_EDRPOU).orElseGet(() -> {
            Fund fund = new Fund();
            fund.setFundName("Demo Fund");
            fund.setEdrpou(FUND_EDRPOU);
            fund.setBankDetail("Demo bank details");
            fund.setRegisteredAddress("Kyiv");
            fund.setActualAddress("Kyiv");
            fund.setPhoneNumber("+380501112233");
            fund.setCorpEmail("fund@helpbridge.test");
            fund.setWebsite("https://helpbridge.test");
            fund.setStatus(FundStatus.APPROVED);
            return fundRepository.save(fund);
        });
    }

    private void seedRepresentative(String hash, Fund fund) {
        if (!fundRepresentativeRepository.existsByEmail(REPRESENTATIVE_EMAIL)) {
            fundRepresentativeRepository.save(new FundRepresentative(
                    "Test", "Representative", REPRESENTATIVE_EMAIL, "+380501234567", hash, fund));
        }
    }

    private void seedVolunteer(String hash, Fund fund) {
        if (volunteerRepository.findByEmail(VOLUNTEER_EMAIL).isEmpty()) {
            Volunteer volunteer = new Volunteer();
            volunteer.setFund(fund);
            volunteer.setFirstName("Test");
            volunteer.setLastName("Volunteer");
            volunteer.setEmail(VOLUNTEER_EMAIL);
            volunteer.setPhone("+380671112233");
            volunteer.setStatus(VolunteerStatus.ACTIVE);
            volunteer.setPasswordHash(hash);
            volunteerRepository.save(volunteer);
        }
    }
}
