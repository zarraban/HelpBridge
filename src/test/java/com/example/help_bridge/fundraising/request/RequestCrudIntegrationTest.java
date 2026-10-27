package com.example.help_bridge.fundraising.request;

import com.example.help_bridge.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@WithMockUser(roles = "APPLICANT")
class RequestCrudIntegrationTest {

    private static final String URL = "/api/v1/requests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    private RequestTestData data;
    private long userId;

    @BeforeEach
    void setUp() {
        data = new RequestTestData(jdbc);
        userId = data.insertUser("applicant@request.test");
    }

    @Test
    void createRequest_ShouldInsertRowIntoRequestsTable() throws Exception {
        String deadline = LocalDate.now().plusMonths(2).toString();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(userId, "MEDICAL", "15000.00", deadline)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(URL + "/1")))
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.requesterId").value(userId));

        Map<String, Object> row = jdbc.sql("""
                        select assistance_type_code, amount, status, institution_name,
                               application_number, data_processing_consent, user_id, fund_id
                        from requests""")
                .query()
                .singleRow();
        assertThat(row.get("assistance_type_code")).isEqualTo("MEDICAL");
        assertThat((BigDecimal) row.get("amount")).isEqualByComparingTo("15000.00");
        assertThat(row.get("status")).isEqualTo("PENDING_VERIFICATION");
        assertThat(row.get("institution_name")).isEqualTo("Київська міська лікарня №1");
        assertThat(row.get("application_number")).isEqualTo("APP-2026-0001");
        assertThat(row.get("data_processing_consent")).isEqualTo(true);
        assertThat(row.get("user_id")).isEqualTo(userId);
        assertThat(row.get("fund_id")).isNull();
    }

    @Test
    void createRequest_WithNegativeAmount_ShouldReturn400AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(userId, "MEDICAL", "-5.00",
                                LocalDate.now().plusMonths(2).toString())))
                .andExpect(status().isBadRequest());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void createRequest_WithPastDeadline_ShouldReturn400AndKeepRowCount() throws Exception {
        data.insertRequest(userId, "PENDING_VERIFICATION");
        long before = data.count("requests");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(userId, "MEDICAL", "15000.00",
                                LocalDate.now().minusDays(1).toString())))
                .andExpect(status().isBadRequest());

        assertThat(data.count("requests")).isEqualTo(before);
    }

    @Test
    void createRequest_WithoutConsent_ShouldReturn400AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "assistanceType": "MEDICAL",
                                  "amount": 15000.00,
                                  "deadline": "%s",
                                  "situationDescription": "Ситуація",
                                  "needDescription": "Потреба",
                                  "institutionName": "Лікарня",
                                  "applicationNumber": "APP-1",
                                  "dataProcessingConsent": false
                                }
                                """.formatted(userId, LocalDate.now().plusMonths(1))))
                .andExpect(status().isBadRequest());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void createRequest_WithBlankFields_ShouldReturn400AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void createRequest_WithUnknownAssistanceType_ShouldReturn400AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(userId, "NO_SUCH_TYPE", "15000.00",
                                LocalDate.now().plusMonths(2).toString())))
                .andExpect(status().isBadRequest());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void createRequest_WithUnknownUser_ShouldReturn404AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(9999L, "MEDICAL", "15000.00",
                                LocalDate.now().plusMonths(2).toString())))
                .andExpect(status().isNotFound());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void getRequestById_ShouldReturnRequestFromTable() throws Exception {
        long id = data.insertRequest(userId, "NEW");

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.assistanceType").value("MEDICAL"))
                .andExpect(jsonPath("$.amount").value(15000.00));
    }

    @Test
    void getRequestById_WhenMissing_ShouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRequests_ShouldReturnEveryRow() throws Exception {
        data.insertRequest(userId, "PENDING_VERIFICATION");
        data.insertRequest(userId, "NEW");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getAllRequests_WithStatusFilter_ShouldReturnOnlyMatchingRows() throws Exception {
        data.insertRequest(userId, "PENDING_VERIFICATION");
        data.insertRequest(userId, "NEW");

        mockMvc.perform(get(URL).param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("NEW"));
    }

    @Test
    void getRequests_ByRequesterId_ShouldReturnOnlyOwnRequests() throws Exception {
        long otherUser = data.insertUser("other@request.test");
        data.insertRequest(userId, "NEW");
        data.insertRequest(otherUser, "NEW");

        mockMvc.perform(get(URL).param("requesterId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].requesterId").value(userId));
    }

    @Test
    void updateRequest_ShouldChangeRowInTable() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(put(URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("SURGERY", "20000.00",
                                LocalDate.now().plusMonths(4).toString())))
                .andExpect(status().isOk());

        Map<String, Object> row = jdbc.sql("""
                        select assistance_type_code, amount, institution_name, application_number
                        from requests where id = ?""")
                .param(id)
                .query()
                .singleRow();
        assertThat(row.get("assistance_type_code")).isEqualTo("SURGERY");
        assertThat((BigDecimal) row.get("amount")).isEqualByComparingTo("20000.00");
        assertThat(row.get("institution_name")).isEqualTo("Нова лікарня");
        assertThat(row.get("application_number")).isEqualTo("APP-2026-0002");
    }

    @Test
    void updateRequest_WhenNotPendingVerification_ShouldReturn409AndKeepRow() throws Exception {
        long id = data.insertRequest(userId, "NEW");

        mockMvc.perform(put(URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("SURGERY", "20000.00",
                                LocalDate.now().plusMonths(4).toString())))
                .andExpect(status().isConflict());

        Map<String, Object> row = jdbc.sql(
                        "select assistance_type_code, amount from requests where id = ?")
                .param(id)
                .query()
                .singleRow();
        assertThat(row.get("assistance_type_code")).isEqualTo("MEDICAL");
        assertThat((BigDecimal) row.get("amount")).isEqualByComparingTo("15000.00");
    }

    @Test
    void updateRequest_WithInvalidAmount_ShouldReturn400AndKeepRow() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(put(URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("SURGERY", "0",
                                LocalDate.now().plusMonths(4).toString())))
                .andExpect(status().isBadRequest());

        assertThat(jdbc.sql("select assistance_type_code from requests where id = ?")
                .param(id).query(String.class).single()).isEqualTo("MEDICAL");
    }

    @Test
    void deleteRequest_ShouldRemoveRowFromTable() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(delete(URL + "/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(data.count("requests")).isZero();
    }

    @Test
    void deleteRequest_WhenInProgress_ShouldReturn409AndKeepRow() throws Exception {
        long fundId = data.insertFund("90000001", "APPROVED");
        long id = data.insertRequest(userId, "IN_PROGRESS", fundId);

        mockMvc.perform(delete(URL + "/{id}", id))
                .andExpect(status().isConflict());

        assertThat(data.count("requests")).isEqualTo(1);
        assertThat(data.requestStatus(id)).isEqualTo("IN_PROGRESS");
    }

    @Test
    void deleteRequest_ShouldCascadeToDocuments() throws Exception {
        long id = data.insertRequest(userId, "PENDING_VERIFICATION");
        data.insertDocument(id, "a.pdf", "https://files.test/a.pdf");
        data.insertDocument(id, "b.pdf", "https://files.test/b.pdf");

        mockMvc.perform(delete(URL + "/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(data.count("requests")).isZero();
        assertThat(data.count("request_documents")).isZero();
    }

    @Test
    void addDocument_ShouldInsertRowIntoRequestDocumentsTable() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(post(URL + "/{id}/documents", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentBody("passport.pdf", "https://files.test/passport.pdf")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(URL + "/" + requestId + "/documents/1")))
                .andExpect(jsonPath("$.fileName").value("passport.pdf"));

        Map<String, Object> row = jdbc.sql("""
                        select file_name, file_url, request_id from request_documents""")
                .query()
                .singleRow();
        assertThat(row.get("file_name")).isEqualTo("passport.pdf");
        assertThat(row.get("file_url")).isEqualTo("https://files.test/passport.pdf");
        assertThat(row.get("request_id")).isEqualTo(requestId);
    }

    @Test
    void addDocument_ToMissingRequest_ShouldReturn404AndKeepTableEmpty() throws Exception {
        mockMvc.perform(post(URL + "/{id}/documents", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentBody("passport.pdf", "https://files.test/passport.pdf")))
                .andExpect(status().isNotFound());

        assertThat(data.count("request_documents")).isZero();
    }

    @Test
    void addDocument_WithBlankFileName_ShouldReturn400AndKeepTableEmpty() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(post(URL + "/{id}/documents", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentBody("", "https://files.test/passport.pdf")))
                .andExpect(status().isBadRequest());

        assertThat(data.count("request_documents")).isZero();
    }

    @Test
    void getDocuments_ShouldReturnOnlyDocumentsOfRequest() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");
        long otherRequestId = data.insertRequest(userId, "PENDING_VERIFICATION");
        data.insertDocument(requestId, "a.pdf", "https://files.test/a.pdf");
        data.insertDocument(requestId, "b.pdf", "https://files.test/b.pdf");
        data.insertDocument(otherRequestId, "c.pdf", "https://files.test/c.pdf");

        mockMvc.perform(get(URL + "/{id}/documents", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getDocument_ShouldReturnDocumentFromTable() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");
        long documentId = data.insertDocument(requestId, "a.pdf", "https://files.test/a.pdf");

        mockMvc.perform(get(URL + "/{id}/documents/{docId}", requestId, documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId))
                .andExpect(jsonPath("$.fileName").value("a.pdf"))
                .andExpect(jsonPath("$.fileUrl").value("https://files.test/a.pdf"));
    }

    @Test
    void getDocument_WhenMissing_ShouldReturn404() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");

        mockMvc.perform(get(URL + "/{id}/documents/{docId}", requestId, 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDocument_ShouldChangeRowInTable() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");
        long documentId = data.insertDocument(requestId, "old.pdf", "https://files.test/old.pdf");

        mockMvc.perform(put(URL + "/{id}/documents/{docId}", requestId, documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentBody("new.pdf", "https://files.test/new.pdf")))
                .andExpect(status().isOk());

        Map<String, Object> row = jdbc.sql(
                        "select file_name, file_url from request_documents where id = ?")
                .param(documentId)
                .query()
                .singleRow();
        assertThat(row.get("file_name")).isEqualTo("new.pdf");
        assertThat(row.get("file_url")).isEqualTo("https://files.test/new.pdf");
    }

    @Test
    void deleteDocument_ShouldRemoveRowFromTable() throws Exception {
        long requestId = data.insertRequest(userId, "PENDING_VERIFICATION");
        long documentId = data.insertDocument(requestId, "a.pdf", "https://files.test/a.pdf");

        mockMvc.perform(delete(URL + "/{id}/documents/{docId}", requestId, documentId))
                .andExpect(status().isNoContent());

        assertThat(data.count("request_documents")).isZero();
        assertThat(data.count("requests")).isEqualTo(1);
    }

    private static String createBody(Long userId, String type, String amount, String deadline) {
        return """
                {
                  "userId": %d,
                  "assistanceType": "%s",
                  "amount": %s,
                  "deadline": "%s",
                  "situationDescription": "Потрібне термінове лікування після травми",
                  "needDescription": "Оплата операції та реабілітації",
                  "institutionName": "Київська міська лікарня №1",
                  "applicationNumber": "APP-2026-0001",
                  "dataProcessingConsent": true
                }
                """.formatted(userId, type, amount, deadline);
    }

    private static String updateBody(String type, String amount, String deadline) {
        return """
                {
                  "assistanceType": "%s",
                  "amount": %s,
                  "deadline": "%s",
                  "situationDescription": "Оновлений опис ситуації",
                  "needDescription": "Оновлений опис потреби",
                  "institutionName": "Нова лікарня",
                  "applicationNumber": "APP-2026-0002"
                }
                """.formatted(type, amount, deadline);
    }

    private static String documentBody(String fileName, String fileUrl) {
        return """
                {"fileName": "%s", "fileUrl": "%s"}
                """.formatted(fileName, fileUrl);
    }
}