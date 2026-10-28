package com.example.help_bridge.users.e2e;

import com.example.help_bridge.common.security.dto.request.LoginRequest;
import com.example.help_bridge.users.UsersFixtures;
import com.example.help_bridge.common.security.dto.response.AuthResponse;
import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeRequest;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
class UsersAuthE2EIT {

    private static final String ADMIN = "admin@e2e.test";
    private static final String REPRESENTATIVE = "rep@e2e.test";
    private static final String VOLUNTEER = "volunteer@e2e.test";
    private static final String APPLICANT = "applicant@e2e.test";

    @Autowired RestTestClient client;
    @Autowired PasswordEncoder encoder;
    @Autowired FundRepository funds;
    @Autowired SystemAdminRepository admins;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired VolunteerRepository volunteers;
    @Autowired UserRepository applicants;

    UsersFixtures fx;
    Fund fund;
    Map<AccountType, String> emails = Map.of(
            AccountType.SYSTEM_ADMIN, ADMIN,
            AccountType.FUND_REPRESENTATIVE, REPRESENTATIVE,
            AccountType.VOLUNTEER, VOLUNTEER,
            AccountType.APPLICANT, APPLICANT);

    @BeforeEach
    void setUp() {
        fx = new UsersFixtures(client, encoder, funds, admins, representatives, volunteers, applicants);
        fund = fx.fund();
        fx.admin(ADMIN);
        fx.representative(fund, REPRESENTATIVE);
        fx.volunteer(fund, VOLUNTEER, "+380671112233", VolunteerStatus.ACTIVE);
        fx.applicant(APPLICANT);
    }

    private String tokenFor(AccountType type) {
        return fx.login(type, emails.get(type));
    }
    @ParameterizedTest
    @CsvSource({"SYSTEM_ADMIN", "FUND_REPRESENTATIVE", "VOLUNTEER", "APPLICANT"})
    void login_withValidCredentials_returnsBearerJwt(AccountType type) {
        AuthResponse auth = fx.loginRaw(new LoginRequest(type, emails.get(type), UsersFixtures.PASSWORD))
                .expectStatus().isOk()
                .expectBody(AuthResponse.class).returnResult().getResponseBody();

        assertThat(auth.tokenType()).isEqualTo("Bearer");
        assertThat(auth.expiresIn()).isPositive();
        assertThat(auth.accessToken().split("\\.")).hasSize(3);
    }

    @Test
    void login_withWrongPassword_returns401() {
        fx.loginRaw(new LoginRequest(AccountType.SYSTEM_ADMIN, ADMIN, "WrongPass1!"))
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_withUnknownEmail_returns401() {
        fx.loginRaw(new LoginRequest(AccountType.SYSTEM_ADMIN, "nobody@e2e.test", UsersFixtures.PASSWORD))
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_withWrongAccountType_returns401() {
        // адмін існує, але не як волонтер
        fx.loginRaw(new LoginRequest(AccountType.VOLUNTEER, ADMIN, UsersFixtures.PASSWORD))
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_withMissingFields_returns401() {
        fx.loginRaw(Map.of("email", ADMIN)).expectStatus().isUnauthorized();
    }

    @Test
    void login_inactiveVolunteer_returns401() {
        fx.volunteer(fund, "gone@e2e.test", "+380671119999", VolunteerStatus.INACTIVE);

        fx.loginRaw(new LoginRequest(AccountType.VOLUNTEER, "gone@e2e.test", UsersFixtures.PASSWORD))
                .expectStatus().isUnauthorized();
    }

    @Test
    void token_fromLogin_grantsAccessToProtectedEndpoint() {
        String token = tokenFor(AccountType.SYSTEM_ADMIN);

        fx.send(HttpMethod.GET, "/api/system-admins", token, null).expectStatus().isOk();
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/api/system-admins",
            "GET,/api/fund-representatives",
            "GET,/api/donors",
            "GET,/api/funds/1/volunteers"
    })
    void withoutToken_returns401(HttpMethod method, String uri) {
        fx.send(method, uri, null, null).expectStatus().isUnauthorized();
    }

    @Test
    void withGarbageToken_returns401() {
        fx.send(HttpMethod.GET, "/api/system-admins", "garbage.token.value", null).expectStatus().isUnauthorized();
    }
    @ParameterizedTest
    @CsvSource({
            "SYSTEM_ADMIN,200",
            "FUND_REPRESENTATIVE,403",
            "VOLUNTEER,403",
            "APPLICANT,403"
    })
    void systemAdminsList_byRole(AccountType role, int expected) {
        fx.send(HttpMethod.GET, "/api/system-admins", tokenFor(role), null)
                .expectStatus().isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "SYSTEM_ADMIN,200",
            "FUND_REPRESENTATIVE,200",
            "VOLUNTEER,403",
            "APPLICANT,403"
    })
    void fundVolunteersList_byRole(AccountType role, int expected) {
        fx.send(HttpMethod.GET, "/api/funds/" + fund.getId() + "/volunteers", tokenFor(role), null)
                .expectStatus().isEqualTo(expected);
    }

    @Test
    void createRepresentative_byAdmin_returns201() {
        var request = new FundRepresentativeRequest("New", "Rep", "new@e2e.test", "+380501234567", "Pass1234!", fund.getId());

        fx.send(HttpMethod.POST, "/api/fund-representatives", tokenFor(AccountType.SYSTEM_ADMIN), request)
                .expectStatus().isCreated();

        assertThat(representatives.count()).isEqualTo(2);
    }

    @Test
    void createRepresentative_byRepresentative_returns403_andNothingCreated() {
        var request = new FundRepresentativeRequest("New", "Rep", "new@e2e.test", "+380501234567", "Pass1234!", fund.getId());

        fx.send(HttpMethod.POST, "/api/fund-representatives", tokenFor(AccountType.FUND_REPRESENTATIVE), request)
                .expectStatus().isForbidden();

        assertThat(representatives.count()).isEqualTo(1);
    }
}
