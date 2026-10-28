package com.example.help_bridge.users.donor;

import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.users.UsersFixtures;
import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.donor.dto.request.DonorRequest;
import com.example.help_bridge.users.donor.dto.response.DonorResponse;
import com.example.help_bridge.users.donor.repository.DonorRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@E2ETest
class DonorCrudE2EIT {

    private static final String ADMIN = "admin@donor.test";

    @Autowired RestTestClient client;
    @Autowired PasswordEncoder encoder;
    @Autowired FundRepository funds;
    @Autowired SystemAdminRepository admins;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired VolunteerRepository volunteers;
    @Autowired UserRepository applicants;
    @Autowired FundraiserRepository fundraisers;
    @Autowired DonorRepository donors;

    UsersFixtures fx;
    String token;
    Long fundraiserId;

    @BeforeEach
    void setUp() {
        fx = new UsersFixtures(client, encoder, funds, admins, representatives, volunteers, applicants);
        fx.admin(ADMIN);
        token = fx.login(AccountType.SYSTEM_ADMIN, ADMIN);
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setStatus(FundraiserStatus.PENDING_ASSIGNMENT);
        fundraiserId = fundraisers.save(fundraiser).getId();
    }

    private DonorRequest request(String firstName, String email) {
        return new DonorRequest(fundraiserId, firstName, "Shevchenko", email, "+380501112233");
    }

    private DonorResponse create(String firstName, String email) {
        return fx.send(HttpMethod.POST, "/api/donors", token, request(firstName, email))
                .expectStatus().isCreated()
                .expectBody(DonorResponse.class).returnResult().getResponseBody();
    }

    @Test
    void create_persistsDonor() {
        DonorResponse created = create("Taras", "taras@donor.test");

        assertThat(created.id()).isNotNull();
        assertThat(created.fundraiserId()).isEqualTo(fundraiserId);
        assertThat(created.createdAt()).isNotNull();
        assertThat(donors.findById(created.id())).isPresent();
    }

    @Test
    void getById_returnsDonor() {
        DonorResponse created = create("Taras", "taras@donor.test");

        DonorResponse found = fx.send(HttpMethod.GET, "/api/donors/" + created.id(), token, null)
                .expectStatus().isOk()
                .expectBody(DonorResponse.class).returnResult().getResponseBody();

        assertThat(found.email()).isEqualTo("taras@donor.test");
    }

    @Test
    void getAll_returnsCreatedDonors() {
        create("Taras", "taras@donor.test");
        create("Ivan", "ivan@donor.test");

        List<DonorResponse> all = fx.send(HttpMethod.GET, "/api/donors", token, null)
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<DonorResponse>>() {}).returnResult().getResponseBody();

        assertThat(all).extracting(DonorResponse::email)
                .containsExactlyInAnyOrder("taras@donor.test", "ivan@donor.test");
    }

    @Test
    void update_changesFields() {
        DonorResponse created = create("Taras", "taras@donor.test");

        DonorResponse updated = fx.send(HttpMethod.PATCH, "/api/donors/" + created.id(), token,
                        request("Taras-Updated", "new@donor.test"))
                .expectStatus().isOk()
                .expectBody(DonorResponse.class).returnResult().getResponseBody();

        assertThat(updated.firstName()).isEqualTo("Taras-Updated");
        assertThat(donors.findById(created.id()).orElseThrow().getEmail()).isEqualTo("new@donor.test");
    }

    @Test
    void delete_removesDonor_thenGetReturns404() {
        DonorResponse created = create("Taras", "taras@donor.test");

        fx.send(HttpMethod.DELETE, "/api/donors/" + created.id(), token, null).expectStatus().isOk();

        assertThat(donors.count()).isZero();
        fx.send(HttpMethod.GET, "/api/donors/" + created.id(), token, null).expectStatus().isNotFound();
    }

    @Test
    void create_withInvalidBody_returns400_andRowCountUnchanged() {
        long before = donors.count();
        DonorRequest invalid = new DonorRequest(fundraiserId, "T", "Shevchenko", "not-an-email", "123");

        fx.send(HttpMethod.POST, "/api/donors", token, invalid).expectStatus().isBadRequest();

        assertThat(donors.count()).isEqualTo(before);
    }

    @Test
    void create_withUnknownFundraiser_returns404() {
        DonorRequest request = new DonorRequest(999_999L, "Taras", "Shevchenko", "t@donor.test", null);

        fx.send(HttpMethod.POST, "/api/donors", token, request).expectStatus().isNotFound();

        assertThat(donors.count()).isZero();
    }

    @Test
    void getUnknownDonor_returns404() {
        fx.send(HttpMethod.GET, "/api/donors/999999", token, null).expectStatus().isNotFound();
    }
}
