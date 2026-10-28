package com.example.help_bridge.users;

import com.example.help_bridge.common.security.dto.request.LoginRequest;
import com.example.help_bridge.common.security.dto.response.AuthResponse;
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
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.atomic.AtomicInteger;

public final class UsersFixtures {

    public static final String PASSWORD = "Pass1234!";

    private static final AtomicInteger EDRPOU_SEQ = new AtomicInteger(10_000_000);

    private final RestTestClient client;
    private final PasswordEncoder encoder;
    private final FundRepository funds;
    private final SystemAdminRepository admins;
    private final FundRepresentativeRepository representatives;
    private final VolunteerRepository volunteers;
    private final UserRepository applicants;

    public UsersFixtures(RestTestClient client, PasswordEncoder encoder, FundRepository funds,
                         SystemAdminRepository admins, FundRepresentativeRepository representatives,
                         VolunteerRepository volunteers, UserRepository applicants) {
        this.client = client;
        this.encoder = encoder;
        this.funds = funds;
        this.admins = admins;
        this.representatives = representatives;
        this.volunteers = volunteers;
        this.applicants = applicants;
    }

    public Fund fund() {
        Fund fund = new Fund();
        fund.setFundName("Test Fund");
        fund.setEdrpou(String.valueOf(EDRPOU_SEQ.incrementAndGet()));
        fund.setBankDetail("bank");
        fund.setRegisteredAddress("Kyiv");
        fund.setActualAddress("Kyiv");
        fund.setPhoneNumber("+380501112233");
        fund.setCorpEmail("fund@helpbridge.test");
        fund.setWebsite("https://helpbridge.test");
        fund.setStatus(FundStatus.APPROVED);
        return funds.save(fund);
    }

    public SystemAdmin admin(String email) {
        return admins.save(new SystemAdmin(email, encoder.encode(PASSWORD), "Test", "Admin"));
    }

    public FundRepresentative representative(Fund fund, String email) {
        return representatives.save(new FundRepresentative(
                "Test", "Representative", email, "+380501234567", encoder.encode(PASSWORD), fund));
    }

    public Volunteer volunteer(Fund fund, String email, String phone, VolunteerStatus status) {
        Volunteer v = new Volunteer();
        v.setFund(fund);
        v.setFirstName("Test");
        v.setLastName("Volunteer");
        v.setEmail(email);
        v.setPhone(phone);
        v.setStatus(status);
        v.setPasswordHash(encoder.encode(PASSWORD));
        return volunteers.save(v);
    }

    public User applicant(String email) {
        return applicants.save(new User("Test", "Requester", email, encoder.encode(PASSWORD)));
    }

    public RestTestClient.ResponseSpec loginRaw(Object body) {
        return client.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON).body(body).exchange();
    }

    public String login(AccountType type, String email) {
        AuthResponse auth = loginRaw(new LoginRequest(type, email, PASSWORD))
                .expectStatus().isOk()
                .expectBody(AuthResponse.class).returnResult().getResponseBody();
        return auth.accessToken();
    }

    public RestTestClient.ResponseSpec send(HttpMethod method, String uri, String token, Object body) {
        RestTestClient.RequestBodySpec spec = client.method(method).uri(uri);
        if (token != null) {
            spec.headers(h -> h.setBearerAuth(token));
        }
        if (body != null) {
            spec.contentType(MediaType.APPLICATION_JSON);
            spec.body(body);
        }
        return spec.exchange();
    }
}
