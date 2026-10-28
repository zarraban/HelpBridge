package com.example.help_bridge.users.volunteer;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.notification.repository.MailingTaskJpaRepository;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.UsersFixtures;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.example.help_bridge.users.volunteer.dto.request.RegisterVolunteerRequest;
import com.example.help_bridge.users.volunteer.dto.request.VolunteerRequest;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerResponse;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@E2ETest
class VolunteerCrudE2EIT {

    private static final String ADMIN = "admin@vol.test";

    @Autowired RestTestClient client;
    @Autowired PasswordEncoder encoder;
    @Autowired FundRepository funds;
    @Autowired SystemAdminRepository admins;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired VolunteerRepository volunteers;
    @Autowired UserRepository applicants;
    @Autowired MailingTaskJpaRepository mailingTasks;

    UsersFixtures fx;
    String token;
    Fund fund;
    String base;

    @BeforeEach
    void setUp() {
        fx = new UsersFixtures(client, encoder, funds, admins, representatives, volunteers, applicants);
        fx.admin(ADMIN);
        token = fx.login(AccountType.SYSTEM_ADMIN, ADMIN);
        fund = fx.fund();
        base = "/api/funds/" + fund.getId() + "/volunteers";
    }

    private RegisterVolunteerRequest request(String email, String phone) {
        return new RegisterVolunteerRequest("Andrii", "Melnyk", email, phone, "Pass1234!");
    }

    private VolunteerResponse register(String email, String phone) {
        VolunteerResponse created = fx.send(HttpMethod.POST, base, token, request(email, phone))
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(VolunteerResponse.class).returnResult().getResponseBody();
        await().atMost(Duration.ofSeconds(10)).until(() -> mailingTasks.count() > 0);
        return created;
    }

    @Test
    void register_persistsActiveVolunteer_withHashedPassword() {
        VolunteerResponse created = register("andrii@vol.test", "+380671112233");

        var saved = volunteers.findById(created.id()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(VolunteerStatus.ACTIVE);
        assertThat(encoder.matches("Pass1234!", saved.getPasswordHash())).isTrue();
        assertThat(created.fundId()).isEqualTo(fund.getId());
    }

    @Test
    void getById_andGetAll_returnActiveVolunteers() {
        VolunteerResponse created = register("andrii@vol.test", "+380671112233");

        VolunteerResponse found = fx.send(HttpMethod.GET, base + "/" + created.id(), token, null)
                .expectStatus().isOk()
                .expectBody(VolunteerResponse.class).returnResult().getResponseBody();
        List<VolunteerResponse> all = fx.send(HttpMethod.GET, base, token, null)
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<VolunteerResponse>>() {}).returnResult().getResponseBody();

        assertThat(found.email()).isEqualTo("andrii@vol.test");
        assertThat(all).extracting(VolunteerResponse::id).containsExactly(created.id());
    }

    @Test
    void update_changesFields() {
        VolunteerResponse created = register("andrii@vol.test", "+380671112233");
        var update = new VolunteerRequest("Bohdan", "Kravets", "bohdan@vol.test", "+380671112233");

        fx.send(HttpMethod.PUT, base + "/" + created.id(), token, update).expectStatus().isOk();

        var saved = volunteers.findById(created.id()).orElseThrow();
        assertThat(saved.getFirstName()).isEqualTo("Bohdan");
        assertThat(saved.getEmail()).isEqualTo("bohdan@vol.test");
    }

    @Test
    void delete_isSoft_statusInactive_andGetReturns404() {
        VolunteerResponse created = register("andrii@vol.test", "+380671112233");

        fx.send(HttpMethod.DELETE, base + "/" + created.id(), token, null).expectStatus().isNoContent();

        assertThat(volunteers.findById(created.id()).orElseThrow().getStatus()).isEqualTo(VolunteerStatus.INACTIVE);
        fx.send(HttpMethod.GET, base + "/" + created.id(), token, null).expectStatus().isNotFound();
    }

    @Test
    void register_duplicateEmail_returns409_andRowCountUnchanged() {
        register("andrii@vol.test", "+380671112233");
        long before = volunteers.count();

        fx.send(HttpMethod.POST, base, token, request("andrii@vol.test", "+380671119999")).expectStatus().isEqualTo(409);

        assertThat(volunteers.count()).isEqualTo(before);
    }

    @Test
    void register_withInvalidBody_returns400_andRowCountUnchanged() {
        var invalid = new RegisterVolunteerRequest("", "Melnyk", "bad-email", "12345", "short");

        fx.send(HttpMethod.POST, base, token, invalid).expectStatus().isBadRequest();

        assertThat(volunteers.count()).isZero();
    }

    @Test
    void register_inUnknownFund_returns404() {
        fx.send(HttpMethod.POST, "/api/funds/999999/volunteers", token, request("andrii@vol.test", "+380671112233"))
                .expectStatus().isNotFound();

        assertThat(volunteers.count()).isZero();
    }

    @Test
    void getUnknownVolunteer_returns404() {
        fx.send(HttpMethod.GET, base + "/999999", token, null).expectStatus().isNotFound();
    }
}
