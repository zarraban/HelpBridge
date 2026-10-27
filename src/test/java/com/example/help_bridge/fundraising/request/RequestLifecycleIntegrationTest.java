package com.example.help_bridge.fundraising.request;

import com.example.help_bridge.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class RequestLifecycleIntegrationTest {

    private static final String URL = "/api/v1/requests";

    private static final RequestPostProcessor APPLICANT = user("applicant").roles("APPLICANT");
    private static final RequestPostProcessor ADMIN = user("admin").roles("SYSTEM_ADMIN");
    private static final RequestPostProcessor FUND_REP = user("rep").roles("FUND_REPRESENTATIVE");
    private static final RequestPostProcessor VOLUNTEER = user("volunteer").roles("VOLUNTEER");
    private static final RequestPostProcessor DONOR = user("donor").roles("DONOR");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    private RequestTestData data;
    private long userId;

    @BeforeEach
    void setUp() {
        data = new RequestTestData(jdbc);
        userId = data.insertUser("applicant@lifecycle.test");
    }

    @Test
    void fullFlow_CreateVerifyBook_ShouldMoveRequestThroughStatusesInTable() throws Exception {
        long fundId = data.insertFund("70000001", "APPROVED");

        mockMvc.perform(post(URL).with(APPLICANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"));
        long requestId = jdbc.sql("select id from requests").query(Long.class).single();
        assertThat(data.requestStatus(requestId)).isEqualTo("PENDING_VERIFICATION");
        assertThat(data.requestFundId(requestId)).isNull();

        mockMvc.perform(patch(URL + "/{id}/verification", requestId).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationBody(true)))
                .andExpect(status().isNoContent());
        assertThat(data.requestStatus(requestId)).isEqualTo("NEW");

        mockMvc.perform(post(URL + "/{id}/fundraiser", requestId).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(fundId)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.fundId").value(fundId));
        assertThat(data.requestStatus(requestId)).isEqualTo("IN_PROGRESS");
        assertThat(data.requestFundId(requestId)).isEqualTo(fundId);
    }

    @Test
    void createRequest_AsApplicant_ShouldReturn201AndSavePendingVerification() throws Exception {
        mockMvc.perform(post(URL).with(APPLICANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());

        assertThat(data.count("requests")).isEqualTo(1);
        long id = jdbc.sql("select id from requests").query(Long.class).single();
        assertThat(data.requestStatus(id)).isEqualTo("PENDING_VERIFICATION");
    }

    @Test
    void createRequest_AsSystemAdmin_ShouldReturn201AndSavePendingVerification() throws Exception {
        mockMvc.perform(post(URL).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());

        assertThat(data.count("requests")).isEqualTo(1);
        long id = jdbc.sql("select id from requests").query(Long.class).single();
        assertThat(data.requestStatus(id)).isEqualTo("PENDING_VERIFICATION");
    }

    @Test
    void createRequest_AsFundRepresentative_ShouldReturn403AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void createRequest_AsVolunteerOrDonor_ShouldReturn403AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL).with(VOLUNTEER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(URL).with(DONOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void createRequest_Anonymous_ShouldBeRejectedAndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().is4xxClientError());

        assertThat(data.count("requests")).isZero();
    }


    @Test
    void approveRequest_AsSystemAdmin_ShouldSetStatusNew() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(patch(URL + "/{id}/verification", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationBody(true)))
                .andExpect(status().isNoContent());

        assertThat(data.requestStatus(id)).isEqualTo("NEW");
    }

    @Test
    void rejectRequest_AsSystemAdmin_ShouldSetStatusRejected() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(patch(URL + "/{id}/verification", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationBody(false)))
                .andExpect(status().isNoContent());

        assertThat(data.count("requests")).isEqualTo(1);
        assertThat(data.requestStatus(id)).isEqualTo("REJECTED");
    }

    @Test
    void bookRequest_WhenRejected_ShouldReturn409AndKeepStatus() throws Exception {
        long fundId = data.insertFund("80000009", "APPROVED");
        long id = data.insertRequest(userId, "REJECTED");

        mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(fundId)))
                .andExpect(status().isConflict());

        assertThat(data.requestStatus(id)).isEqualTo("REJECTED");
        assertThat(data.requestFundId(id)).isNull();
    }

    @Test
    void verifyRequest_AsNonAdminRoles_ShouldReturn403AndKeepStatus() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        for (RequestPostProcessor role : new RequestPostProcessor[]{APPLICANT, FUND_REP, VOLUNTEER, DONOR}) {
            mockMvc.perform(patch(URL + "/{id}/verification", id).with(role)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(verificationBody(true)))
                    .andExpect(status().isForbidden());
        }

        assertThat(data.requestStatus(id)).isEqualTo("PENDING_VERIFICATION");
    }

    @Test
    void verifyRequest_WhenAlreadyVerified_ShouldReturn409AndKeepStatus() throws Exception {
        long id = data.insertRequest(userId, "NEW");

        mockMvc.perform(patch(URL + "/{id}/verification", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationBody(true)))
                .andExpect(status().isConflict());

        assertThat(data.requestStatus(id)).isEqualTo("NEW");
    }

    @Test
    void verifyRequest_WhenMissing_ShouldReturn404() throws Exception {
        mockMvc.perform(patch(URL + "/{id}/verification", 9999).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationBody(true)))
                .andExpect(status().isNotFound());
    }

    @Test
    void bookRequest_AsFundRepresentative_ShouldSetInProgressAndFund() throws Exception {
        long fundId = data.insertFund("80000001", "APPROVED");
        long id = data.insertRequest(userId, "NEW");

        mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(fundId)))
                .andExpect(status().isAccepted());

        assertThat(data.requestStatus(id)).isEqualTo("IN_PROGRESS");
        assertThat(data.requestFundId(id)).isEqualTo(fundId);
    }

    @Test
    void bookRequest_AsSystemAdmin_ShouldSetInProgressAndFund() throws Exception {
        long fundId = data.insertFund("80000002", "APPROVED");
        long id = data.insertRequest(userId, "NEW");

        mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(fundId)))
                .andExpect(status().isAccepted());

        assertThat(data.requestStatus(id)).isEqualTo("IN_PROGRESS");
    }

    @Test
    void bookRequest_AsApplicantVolunteerOrDonor_ShouldReturn403AndKeepStatus() throws Exception {
        long fundId = data.insertFund("80000003", "APPROVED");
        long id = data.insertRequest(userId, "NEW");

        for (RequestPostProcessor role : new RequestPostProcessor[]{APPLICANT, VOLUNTEER, DONOR}) {
            mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(role)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bookingBody(fundId)))
                    .andExpect(status().isForbidden());
        }

        assertThat(data.requestStatus(id)).isEqualTo("NEW");
        assertThat(data.requestFundId(id)).isNull();
    }

    @Test
    void bookRequest_WithNotApprovedFund_ShouldReturn409AndKeepStatus() throws Exception {
        long fundId = data.insertFund("80000004", "PENDING_APPROVAL");
        long id = data.insertRequest(userId, "NEW");

        mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(fundId)))
                .andExpect(status().isConflict());

        assertThat(data.requestStatus(id)).isEqualTo("NEW");
        assertThat(data.requestFundId(id)).isNull();
    }

    @Test
    void bookRequest_WhenNotVerified_ShouldReturn409AndKeepStatus() throws Exception {
        long fundId = data.insertFund("80000005", "APPROVED");
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(fundId)))
                .andExpect(status().isConflict());

        assertThat(data.requestStatus(id)).isEqualTo("PENDING_VERIFICATION");
        assertThat(data.requestFundId(id)).isNull();
    }

    @Test
    void bookRequest_WhenAlreadyBooked_ShouldReturn409AndKeepFirstFund() throws Exception {
        long firstFund = data.insertFund("80000006", "APPROVED");
        long secondFund = data.insertFund("80000007", "APPROVED");
        long id = data.insertRequest(userId, "IN_PROGRESS", firstFund);

        mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(secondFund)))
                .andExpect(status().isConflict());

        assertThat(data.requestFundId(id)).isEqualTo(firstFund);
    }

    @Test
    void bookRequest_WithMissingFundOrRequest_ShouldReturn404() throws Exception {
        long fundId = data.insertFund("80000008", "APPROVED");
        long id = data.insertRequest(userId, "NEW");

        mockMvc.perform(post(URL + "/{id}/fundraiser", id).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(9999L)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(URL + "/{id}/fundraiser", 9999).with(FUND_REP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(fundId)))
                .andExpect(status().isNotFound());

        assertThat(data.requestStatus(id)).isEqualTo("NEW");
    }

    @Test
    void sharedPool_ShouldBeAvailableOnlyToFundRepresentativeAndAdmin() throws Exception {
        data.insertRequest(userId, "NEW");

        mockMvc.perform(get(URL + "/shared-pool").with(FUND_REP)).andExpect(status().isOk());
        mockMvc.perform(get(URL + "/shared-pool").with(ADMIN)).andExpect(status().isOk());
        mockMvc.perform(get(URL + "/shared-pool").with(APPLICANT)).andExpect(status().isForbidden());
        mockMvc.perform(get(URL + "/shared-pool").with(VOLUNTEER)).andExpect(status().isForbidden());
    }

    private String createBody() {
        return """
                {
                  "userId": %d,
                  "assistanceType": "MEDICAL",
                  "amount": 15000.00,
                  "deadline": "%s",
                  "situationDescription": "Потрібне термінове лікування після травми",
                  "needDescription": "Оплата операції та реабілітації",
                  "institutionName": "Київська міська лікарня №1",
                  "applicationNumber": "APP-2026-0001",
                  "dataProcessingConsent": true
                }
                """.formatted(userId, LocalDate.now().plusMonths(2));
    }

    private static String verificationBody(boolean approved) {
        return """
                {"approved": %s}
                """.formatted(approved);
    }

    private static String bookingBody(long fundId) {
        return """
                {"fundId": %d}
                """.formatted(fundId);
    }
}