package com.example.help_bridge.users.systemadmin;
import com.example.help_bridge.users.UsersFixtures;
import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.fundraising.verification.entity.VerificationAct;
import com.example.help_bridge.fundraising.verification.repository.VerificationActRepository;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminUpdateRequest;
import com.example.help_bridge.users.systemadmin.dto.response.SystemAdminResponse;
import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
class SystemAdminCrudE2EIT {

    private static final String BASE = "/api/system-admins";
    private static final String ADMIN = "root@admin.test";

    @Autowired RestTestClient client;
    @Autowired PasswordEncoder encoder;
    @Autowired FundRepository funds;
    @Autowired SystemAdminRepository admins;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired VolunteerRepository volunteers;
    @Autowired UserRepository applicants;
    @Autowired VerificationActRepository verificationActs;

    UsersFixtures fx;
    String token;

    @BeforeEach
    void setUp() {
        fx = new UsersFixtures(client, encoder, funds, admins, representatives, volunteers, applicants);
        fx.admin(ADMIN);
        token = fx.login(AccountType.SYSTEM_ADMIN, ADMIN);
    }

    private SystemAdminCreateRequest request(String email, String lastName) {
        return new SystemAdminCreateRequest(email, "Pass1234!", "Oleksandr", lastName);
    }

    private SystemAdminResponse create(String email, String lastName) {
        return fx.send(HttpMethod.POST, BASE, token, request(email, lastName))
                .expectStatus().isCreated()
                .expectBody(SystemAdminResponse.class).returnResult().getResponseBody();
    }

    @Test
    void create_persistsAdmin_withHashedPassword() {
        SystemAdminResponse created = create("new@admin.test", "Ivanenko");

        SystemAdmin saved = admins.findById(created.id()).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("Pass1234!");
        assertThat(encoder.matches("Pass1234!", saved.getPasswordHash())).isTrue();
    }

    @Test
    void getById_andGetAll_andSearch() {
        SystemAdminResponse created = create("new@admin.test", "Ivanenko");

        SystemAdminResponse found = fx.send(HttpMethod.GET, BASE + "/" + created.id(), token, null)
                .expectStatus().isOk()
                .expectBody(SystemAdminResponse.class).returnResult().getResponseBody();
        List<SystemAdminResponse> all = fx.send(HttpMethod.GET, BASE, token, null)
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<SystemAdminResponse>>() {}).returnResult().getResponseBody();
        List<SystemAdminResponse> searched = fx.send(HttpMethod.GET, BASE + "/search?lastName=ivan", token, null)
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<SystemAdminResponse>>() {}).returnResult().getResponseBody();

        assertThat(found.email()).isEqualTo("new@admin.test");
        assertThat(all).hasSize(2);
        assertThat(searched).extracting(SystemAdminResponse::id).containsExactly(created.id());
    }

    @Test
    void update_changesNames() {
        SystemAdminResponse created = create("new@admin.test", "Ivanenko");

        fx.send(HttpMethod.PUT, BASE + "/" + created.id(), token, new SystemAdminUpdateRequest("Petro", "Shevchuk"))
                .expectStatus().isOk();

        SystemAdmin saved = admins.findById(created.id()).orElseThrow();
        assertThat(saved.getFirstName()).isEqualTo("Petro");
        assertThat(saved.getLastName()).isEqualTo("Shevchuk");
    }

    @Test
    void delete_removesAdmin_thenGetReturns404() {
        SystemAdminResponse created = create("new@admin.test", "Ivanenko");

        fx.send(HttpMethod.DELETE, BASE + "/" + created.id(), token, null).expectStatus().isNoContent();

        assertThat(admins.existsById(created.id())).isFalse();
        fx.send(HttpMethod.GET, BASE + "/" + created.id(), token, null).expectStatus().isNotFound();
    }

    @Test
    void delete_adminWithVerificationActs_returns409_andAdminStays() {
        SystemAdminResponse created = create("new@admin.test", "Ivanenko");
        Fund fund = fx.fund();
        SystemAdmin admin = admins.findById(created.id()).orElseThrow();
        verificationActs.save(new VerificationAct(fund, admin, LocalDate.now(), FundStatus.APPROVED, "ok"));

        fx.send(HttpMethod.DELETE, BASE + "/" + created.id(), token, null).expectStatus().isEqualTo(409);

        assertThat(admins.existsById(created.id())).isTrue();
    }

    @Test
    void create_duplicateEmail_returns409_andRowCountUnchanged() {
        create("new@admin.test", "Ivanenko");
        long before = admins.count();

        fx.send(HttpMethod.POST, BASE, token, request("new@admin.test", "Other")).expectStatus().isEqualTo(409);

        assertThat(admins.count()).isEqualTo(before);
    }

    @Test
    void create_withInvalidBody_returns400_andRowCountUnchanged() {
        long before = admins.count();
        var invalid = new SystemAdminCreateRequest("bad-email", "short", "", "I");

        fx.send(HttpMethod.POST, BASE, token, invalid).expectStatus().isBadRequest();

        assertThat(admins.count()).isEqualTo(before);
    }

    @Test
    void getUnknownAdmin_returns404() {
        fx.send(HttpMethod.GET, BASE + "/999999", token, null).expectStatus().isNotFound();
    }
}
