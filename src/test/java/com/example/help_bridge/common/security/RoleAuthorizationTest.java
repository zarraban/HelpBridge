package com.example.help_bridge.common.security;

import com.example.help_bridge.common.security.service.JwtService;
import com.example.help_bridge.fundraising.fund.entity.Fund;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "security.jwt.secret=test-secret-test-secret-test-secret-0123456789")
@AutoConfigureMockMvc
class RoleAuthorizationTest {

    private static final String ADMIN_EMAIL = "admin@roles.test";
    private static final String REP_EMAIL = "rita@roles.test";
    private static final String VOLUNTEER_EMAIL = "vera@roles.test";
    private static final String APPLICANT_EMAIL = "applicant@roles.test";

    private static final Set<AccountType> ADMIN = EnumSet.of(AccountType.SYSTEM_ADMIN);
    private static final Set<AccountType> ADMIN_OR_REP = EnumSet.of(AccountType.SYSTEM_ADMIN, AccountType.FUND_REPRESENTATIVE);
    private static final Set<AccountType> VOLUNTEER_OR_REP = EnumSet.of(AccountType.VOLUNTEER, AccountType.FUND_REPRESENTATIVE);
    private static final Set<AccountType> ADMIN_OR_APPLICANT = EnumSet.of(AccountType.SYSTEM_ADMIN, AccountType.APPLICANT);
    private static final Set<AccountType> ANY_ACCOUNT = EnumSet.allOf(AccountType.class);

    private static final String ID = "9999";

    record Endpoint(HttpMethod method, String path, String body, Set<AccountType> allowed) {
        @Override
        public String toString() {
            return method + " " + path + " " + allowed;
        }
    }

    static Stream<Endpoint> endpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.GET, "/api/system-admins", null, ADMIN),
                new Endpoint(HttpMethod.GET, "/api/system-admins/" + ID, null, ADMIN),
                new Endpoint(HttpMethod.POST, "/api/system-admins", """
                        {"email":"new.admin@roles.test","password":"NewAdmin123!","firstName":"New","lastName":"Admin"}""", ADMIN),
                new Endpoint(HttpMethod.DELETE, "/api/system-admins/" + ID, null, ADMIN),

                new Endpoint(HttpMethod.GET, "/api/fund-representatives", null, ADMIN),
                new Endpoint(HttpMethod.POST, "/api/fund-representatives", """
                        {"firstName":"New","lastName":"Rep","email":"new.rep@roles.test","phone":"+380501234567","password":"RepPass123!","fundId":9999}""", ADMIN),
                new Endpoint(HttpMethod.DELETE, "/api/fund-representatives/" + ID, null, ADMIN),

                new Endpoint(HttpMethod.GET, "/api/funds", null, ANY_ACCOUNT),
                new Endpoint(HttpMethod.GET, "/api/funds/" + ID, null, ANY_ACCOUNT),
                new Endpoint(HttpMethod.POST, "/api/funds", """
                        {"fundName":"Roles Fund","edrpou":"87654321","bankDetail":"UA00","registeredAddress":"Kyiv","actualAddress":"Kyiv",
                         "phoneNumber":"+380501112233","corpEmail":"fund@roles.test","website":"https://roles.test","socialMediaUrls":{}}""", ADMIN),
                new Endpoint(HttpMethod.PATCH, "/api/funds/" + ID + "/status", """
                        {"status":"APPROVED","adminId":9999}""", ADMIN),
                new Endpoint(HttpMethod.DELETE, "/api/funds/" + ID, null, ADMIN),

                new Endpoint(HttpMethod.PATCH, "/api/v1/requests/" + ID + "/verification", """
                        {"approved":true}""", ADMIN),
                new Endpoint(HttpMethod.POST, "/api/v1/requests/" + ID + "/fundraiser", """
                        {"fundId":9999}""", ADMIN_OR_REP),

                new Endpoint(HttpMethod.GET, "/api/funds/" + ID + "/volunteers", null, ADMIN_OR_REP),
                new Endpoint(HttpMethod.GET, "/api/funds/" + ID + "/volunteers/" + ID, null, ADMIN_OR_REP),
                new Endpoint(HttpMethod.POST, "/api/funds/" + ID + "/volunteers", """
                        {"firstName":"New","lastName":"Vol","email":"new.vol@roles.test","phone":"+380671112233","password":"VolPass123!"}""", ADMIN_OR_REP),
                new Endpoint(HttpMethod.DELETE, "/api/funds/" + ID + "/volunteers/" + ID, null, ADMIN_OR_REP),

                new Endpoint(HttpMethod.POST, "/api/fundraiser/" + ID + "/close", null, ADMIN_OR_REP),
                new Endpoint(HttpMethod.POST, "/api/fundraiser/mail", """
                        {"fundraiserId":9999,"subjectOfMail":"Subject","message":"Message"}""", ADMIN_OR_REP),
                new Endpoint(HttpMethod.POST, "/api/fundraisers/" + ID + "/assignments", """
                        {"volunteerId":9999}""", ADMIN_OR_REP),

                new Endpoint(HttpMethod.PATCH, "/api/fundraiser-assignments/" + ID + "/complete", """
                        {"closingComment":"Everything was delivered"}""", VOLUNTEER_OR_REP),
                new Endpoint(HttpMethod.PATCH, "/api/fundraiser-assignments/" + ID + "/return", """
                        {"returnReason":"Cannot make it in time"}""", VOLUNTEER_OR_REP),
                new Endpoint(HttpMethod.POST, "/api/fundraisers/" + ID + "/evidences", """
                        {"receiptNumber":"1"}""", VOLUNTEER_OR_REP),

                new Endpoint(HttpMethod.GET, "/api/v1/requests", null, ANY_ACCOUNT),
                new Endpoint(HttpMethod.GET, "/api/v1/requests/" + ID, null, ANY_ACCOUNT),
                new Endpoint(HttpMethod.GET, "/api/v1/requests/shared-pool", null, ADMIN_OR_REP),
                new Endpoint(HttpMethod.POST, "/api/v1/requests", """
                        {"userId":9999,"assistanceType":"MEDICAL","amount":1500.00,"deadline":"2099-01-15",
                         "situationDescription":"s","needDescription":"n","institutionName":"inst",
                         "applicationNumber":"A-1","dataProcessingConsent":true}""", ADMIN_OR_APPLICANT),
                new Endpoint(HttpMethod.PUT, "/api/v1/requests/" + ID, """
                        {"assistanceType":"MEDICAL","amount":2000.00,"deadline":"2099-02-01",
                         "situationDescription":"s","needDescription":"n","institutionName":"inst",
                         "applicationNumber":"A-1"}""", ADMIN_OR_APPLICANT),
                new Endpoint(HttpMethod.DELETE, "/api/v1/requests/" + ID, null, ADMIN_OR_APPLICANT),
                new Endpoint(HttpMethod.GET, "/api/v1/requests/" + ID + "/documents", null, ANY_ACCOUNT),
                new Endpoint(HttpMethod.POST, "/api/v1/requests/" + ID + "/documents", """
                        {"fileName":"a.pdf","fileUrl":"http://x/a"}""", ADMIN_OR_APPLICANT),
                new Endpoint(HttpMethod.PUT, "/api/v1/requests/" + ID + "/documents/" + ID, """
                        {"fileName":"a.pdf","fileUrl":"http://x/a"}""", ADMIN_OR_APPLICANT),
                new Endpoint(HttpMethod.DELETE, "/api/v1/requests/" + ID + "/documents/" + ID, null, ADMIN_OR_APPLICANT)
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private SystemAdminRepository systemAdminRepository;

    @Autowired
    private FundRepresentativeRepository fundRepresentativeRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void seedAccounts() {
        if (systemAdminRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }
        Fund fund = new Fund();
        fund.setFundName("Seed Fund");
        fund.setEdrpou("12345678");
        fund.setBankDetail("UA00 TEST");
        fund.setRegisteredAddress("Kyiv");
        fund.setActualAddress("Kyiv");
        fund.setPhoneNumber("+380501112233");
        fund.setCorpEmail("seed@fund.test");
        fund.setWebsite("https://seed.test");
        fund = fundRepository.save(fund);

        systemAdminRepository.save(new SystemAdmin(ADMIN_EMAIL, "hash", "Ada", "Admin"));
        fundRepresentativeRepository.save(new FundRepresentative("Rita", "Rep", REP_EMAIL, "+380501234567", "hash", fund));

        Volunteer volunteer = new Volunteer();
        volunteer.setFund(fund);
        volunteer.setFirstName("Vera");
        volunteer.setLastName("Vol");
        volunteer.setEmail(VOLUNTEER_EMAIL);
        volunteer.setPhone("+380671112233");
        volunteer.setPasswordHash("hash");
        volunteer.setStatus(VolunteerStatus.ACTIVE);
        volunteerRepository.save(volunteer);

        userRepository.save(new User("Anna", "Applicant", APPLICANT_EMAIL, "hash"));
    }

    private String tokenFor(AccountType type) {
        String email = switch (type) {
            case SYSTEM_ADMIN -> ADMIN_EMAIL;
            case FUND_REPRESENTATIVE -> REP_EMAIL;
            case VOLUNTEER -> VOLUNTEER_EMAIL;
            case APPLICANT -> APPLICANT_EMAIL;
        };
        return jwtService.generateToken(userDetailsService.loadUserByUsername(type + ":" + email));
    }

    private int call(Endpoint endpoint, String token) throws Exception {
        MockHttpServletRequestBuilder builder = request(endpoint.method(), endpoint.path());
        if (endpoint.body() != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(builder).andReturn().getResponse().getStatus();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void enforcesAllowedRolesAndRejectsTheRest(Endpoint endpoint) throws Exception {
        List<String> problems = new ArrayList<>();

        int anonymous = call(endpoint, null);
        if (anonymous != 401) {
            problems.add("anonymous got " + anonymous + " (expected 401)");
        }
        for (AccountType type : AccountType.values()) {
            int result = call(endpoint, tokenFor(type));
            boolean allowed = endpoint.allowed().contains(type);
            if (allowed && (result == 401 || result == 403)) {
                problems.add(type + " got " + result + " but should be allowed");
            }
            if (!allowed && result != 403) {
                problems.add(type + " got " + result + " (expected 403)");
            }
        }

        assertThat(problems).as(endpoint.toString()).isEmpty();
    }

    @Test
    void volunteerCannotCreateSystemAdmin() throws Exception {
        long before = systemAdminRepository.count();

        mockMvc.perform(request(HttpMethod.POST, "/api/system-admins")
                        .header("Authorization", "Bearer " + tokenFor(AccountType.VOLUNTEER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"backdoor@roles.test","password":"Backdoor123!","firstName":"Back","lastName":"Door"}"""))
                .andExpect(status().isForbidden());

        assertThat(systemAdminRepository.count()).isEqualTo(before);
        assertThat(systemAdminRepository.existsByEmail("backdoor@roles.test")).isFalse();
    }

    @Test
    void forbiddenResponseIsProblemDetail() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/system-admins")
                        .header("Authorization", "Bearer " + tokenFor(AccountType.VOLUNTEER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.instance").value("/api/system-admins"));
    }

    @Test
    void unauthenticatedResponseIsProblemDetail() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/api/system-admins"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").value("/api/system-admins"));
    }
}