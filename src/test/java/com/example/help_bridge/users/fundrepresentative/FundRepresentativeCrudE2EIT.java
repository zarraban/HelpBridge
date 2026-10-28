package com.example.help_bridge.users.fundrepresentative;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeRequest;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeUpdateRequest;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeResponse;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.help_bridge.users.UsersFixtures;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
class FundRepresentativeCrudE2EIT {

    private static final String BASE = "/api/fund-representatives";
    private static final String ADMIN = "admin@rep.test";

    @Autowired RestTestClient client;
    @Autowired PasswordEncoder encoder;
    @Autowired FundRepository funds;
    @Autowired SystemAdminRepository admins;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired VolunteerRepository volunteers;
    @Autowired UserRepository applicants;

    UsersFixtures fx;
    String adminToken;
    Fund fund;

    @BeforeEach
    void setUp() {
        fx = new UsersFixtures(client, encoder, funds, admins, representatives, volunteers, applicants);
        fx.admin(ADMIN);
        adminToken = fx.login(AccountType.SYSTEM_ADMIN, ADMIN);
        fund = fx.fund();
    }

    private FundRepresentativeRequest request(String email) {
        return new FundRepresentativeRequest("Olena", "Kovalenko", email, "+380501234567", "Pass1234!", fund.getId());
    }

    private FundRepresentativeResponse create(String email) {
        return fx.send(HttpMethod.POST, BASE, adminToken, request(email))
                .expectStatus().isCreated()
                .expectBody(FundRepresentativeResponse.class).returnResult().getResponseBody();
    }

    @Test
    void create_persistsRepresentative_withHashedPassword() {
        FundRepresentativeResponse created = create("olena@rep.test");

        assertThat(created.fundId()).isEqualTo(fund.getId());
        var saved = representatives.findById(created.id()).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("Pass1234!");
        assertThat(encoder.matches("Pass1234!", saved.getPasswordHash())).isTrue();
    }

    @Test
    void getById_returnsRepresentative() {
        FundRepresentativeResponse created = create("olena@rep.test");

        FundRepresentativeResponse found = fx.send(HttpMethod.GET, BASE + "/" + created.id(), adminToken, null)
                .expectStatus().isOk()
                .expectBody(FundRepresentativeResponse.class).returnResult().getResponseBody();

        assertThat(found.email()).isEqualTo("olena@rep.test");
    }

    @Test
    void getAll_andGetByFund_returnCreated() {
        create("one@rep.test");
        create("two@rep.test");

        List<FundRepresentativeResponse> all = fx.send(HttpMethod.GET, BASE, adminToken, null)
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<FundRepresentativeResponse>>() {})
                .returnResult().getResponseBody();
        List<FundRepresentativeResponse> byFund = fx.send(HttpMethod.GET, BASE + "/fund/" + fund.getId(), adminToken, null)
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<FundRepresentativeResponse>>() {})
                .returnResult().getResponseBody();

        assertThat(all).hasSize(2);
        assertThat(byFund).extracting(FundRepresentativeResponse::email)
                .containsExactlyInAnyOrder("one@rep.test", "two@rep.test");
    }

    @Test
    void update_changesFields() {
        FundRepresentativeResponse created = create("olena@rep.test");
        var update = new FundRepresentativeUpdateRequest("Maria", "Bondar", "maria@rep.test", "+380509999999");

        FundRepresentativeResponse updated = fx.send(HttpMethod.PUT, BASE + "/" + created.id(), adminToken, update)
                .expectStatus().isOk()
                .expectBody(FundRepresentativeResponse.class).returnResult().getResponseBody();

        assertThat(updated.firstName()).isEqualTo("Maria");
        assertThat(representatives.findById(created.id()).orElseThrow().getEmail()).isEqualTo("maria@rep.test");
    }

    @Test
    void delete_removesRepresentative_thenGetReturns404() {
        FundRepresentativeResponse created = create("olena@rep.test");

        fx.send(HttpMethod.DELETE, BASE + "/" + created.id(), adminToken, null).expectStatus().isNoContent();

        assertThat(representatives.existsById(created.id())).isFalse();
        fx.send(HttpMethod.GET, BASE + "/" + created.id(), adminToken, null).expectStatus().isNotFound();
    }

    @Test
    void create_duplicateEmail_returns409_andRowCountUnchanged() {
        create("olena@rep.test");
        long before = representatives.count();

        fx.send(HttpMethod.POST, BASE, adminToken, request("olena@rep.test")).expectStatus().isEqualTo(409);

        assertThat(representatives.count()).isEqualTo(before);
    }

    @Test
    void create_withInvalidBody_returns400_andRowCountUnchanged() {
        var invalid = new FundRepresentativeRequest("", "Kovalenko", "bad-email", "+380501234567", "short", fund.getId());

        fx.send(HttpMethod.POST, BASE, adminToken, invalid).expectStatus().isBadRequest();

        assertThat(representatives.count()).isZero();
    }

    @Test
    void create_withUnknownFund_returns404() {
        var request = new FundRepresentativeRequest("Olena", "Kovalenko", "o@rep.test", "+380501234567", "Pass1234!", 999_999L);

        fx.send(HttpMethod.POST, BASE, adminToken, request).expectStatus().isNotFound();

        assertThat(representatives.count()).isZero();
    }
}
