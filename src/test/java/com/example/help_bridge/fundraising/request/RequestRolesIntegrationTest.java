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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@IntegrationTest
class RequestRolesIntegrationTest {

    private static final String URL = "/api/v1/requests";

    private static final RequestPostProcessor APPLICANT = user("applicant").roles("APPLICANT");
    private static final RequestPostProcessor ADMIN = user("admin").roles("SYSTEM_ADMIN");
    private static final RequestPostProcessor[] FORBIDDEN_ROLES = {
            user("rep").roles("FUND_REPRESENTATIVE"),
            user("volunteer").roles("VOLUNTEER"),
            user("donor").roles("DONOR")
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    private RequestTestData data;
    private long userId;

    @BeforeEach
    void setUp() {
        data = new RequestTestData(jdbc);
        userId = data.insertUser("applicant@roles.test");
    }

    @Test
    void updateRequest_AsForbiddenRoles_ShouldReturn403AndKeepRow() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        for (RequestPostProcessor role : FORBIDDEN_ROLES) {
            mockMvc.perform(put(URL + "/{id}", id).with(role)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateBody()))
                    .andExpect(status().isForbidden());
        }

        Map<String, Object> row = jdbc.sql(
                        "select assistance_type_code, institution_name from requests where id = ?")
                .param(id).query().singleRow();
        assertThat(row.get("assistance_type_code")).isEqualTo("MEDICAL");
        assertThat(row.get("institution_name")).isEqualTo("Лікарня №1");
    }

    @Test
    void updateRequest_AsApplicantAndAdmin_ShouldReturn200() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(put(URL + "/{id}", id).with(APPLICANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody()))
                .andExpect(status().isOk());
        mockMvc.perform(put(URL + "/{id}", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody()))
                .andExpect(status().isOk());

        assertThat(jdbc.sql("select assistance_type_code from requests where id = ?")
                .param(id).query(String.class).single()).isEqualTo("SURGERY");
    }

    @Test
    void deleteRequest_AsForbiddenRoles_ShouldReturn403AndKeepRow() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        for (RequestPostProcessor role : FORBIDDEN_ROLES) {
            mockMvc.perform(delete(URL + "/{id}", id).with(role))
                    .andExpect(status().isForbidden());
        }

        assertThat(data.count("requests")).isEqualTo(1);
        assertThat(data.requestStatus(id)).isEqualTo("PENDING_VERIFICATION");
    }

    @Test
    void deleteRequest_AsApplicantAndAdmin_ShouldRemoveRow() throws Exception {
        long first = data.insertRequest(userId, "PENDING_VERIFICATION");
        long second = data.insertRequest(userId, "NEW");

        mockMvc.perform(delete(URL + "/{id}", first).with(APPLICANT))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete(URL + "/{id}", second).with(ADMIN))
                .andExpect(status().isNoContent());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void addDocument_AsForbiddenRoles_ShouldReturn403AndKeepTableEmpty() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");

        for (RequestPostProcessor role : FORBIDDEN_ROLES) {
            mockMvc.perform(post(URL + "/{id}/documents", requestId).with(role)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(documentBody("a.pdf", "https://files.test/a.pdf")))
                    .andExpect(status().isForbidden());
        }

        assertThat(data.count("request_documents")).isZero();
    }

    @Test
    void addDocument_AsApplicantAndAdmin_ShouldReturn201() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(post(URL + "/{id}/documents", requestId).with(APPLICANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentBody("a.pdf", "https://files.test/a.pdf")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(URL + "/{id}/documents", requestId).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentBody("b.pdf", "https://files.test/b.pdf")))
                .andExpect(status().isCreated());

        assertThat(data.count("request_documents")).isEqualTo(2);
    }

    @Test
    void updateDocument_AsForbiddenRoles_ShouldReturn403AndKeepRow() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");
        long documentId = data.insertDocument(requestId, "old.pdf", "https://files.test/old.pdf");

        for (RequestPostProcessor role : FORBIDDEN_ROLES) {
            mockMvc.perform(put(URL + "/{id}/documents/{docId}", requestId, documentId).with(role)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(documentBody("new.pdf", "https://files.test/new.pdf")))
                    .andExpect(status().isForbidden());
        }

        assertThat(jdbc.sql("select file_name from request_documents where id = ?")
                .param(documentId).query(String.class).single()).isEqualTo("old.pdf");
    }

    @Test
    void deleteDocument_AsForbiddenRoles_ShouldReturn403AndKeepRow() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");
        long documentId = data.insertDocument(requestId, "a.pdf", "https://files.test/a.pdf");

        for (RequestPostProcessor role : FORBIDDEN_ROLES) {
            mockMvc.perform(delete(URL + "/{id}/documents/{docId}", requestId, documentId).with(role))
                    .andExpect(status().isForbidden());
        }

        assertThat(data.count("request_documents")).isEqualTo(1);
    }

    @Test
    void readEndpoints_AsAnyAuthenticatedRole_ShouldReturn200() throws Exception {
        long requestId = data.insertRequest(userId, "NEW");
        long documentId = data.insertDocument(requestId, "a.pdf", "https://files.test/a.pdf");

        for (RequestPostProcessor role : FORBIDDEN_ROLES) {
            mockMvc.perform(get(URL).with(role)).andExpect(status().isOk());
            mockMvc.perform(get(URL + "/{id}", requestId).with(role)).andExpect(status().isOk());
            mockMvc.perform(get(URL + "/{id}/documents", requestId).with(role)).andExpect(status().isOk());
            mockMvc.perform(get(URL + "/{id}/documents/{docId}", requestId, documentId).with(role))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void readEndpoints_Anonymous_ShouldBeRejected() throws Exception {
        long requestId = data.insertRequest(userId, "NEW");

        mockMvc.perform(get(URL)).andExpect(status().is4xxClientError());
        mockMvc.perform(get(URL + "/{id}", requestId)).andExpect(status().is4xxClientError());
    }

    private static String updateBody() {
        return """
                {
                  "assistanceType": "SURGERY",
                  "amount": 20000.00,
                  "deadline": "%s",
                  "situationDescription": "Оновлений опис ситуації",
                  "needDescription": "Оновлений опис потреби",
                  "institutionName": "Нова лікарня",
                  "applicationNumber": "APP-2026-0002"
                }
                """.formatted(LocalDate.now().plusMonths(4));
    }

    private static String documentBody(String fileName, String fileUrl) {
        return """
                {"fileName": "%s", "fileUrl": "%s"}
                """.formatted(fileName, fileUrl);
    }
}