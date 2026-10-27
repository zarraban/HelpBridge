package com.example.help_bridge.fundraising.fund;

import com.example.help_bridge.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@WithMockUser(roles = "SYSTEM_ADMIN")
class FundCrudIntegrationTest {

    private static final String URL = "/api/funds";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void createFund_ShouldInsertRowIntoFundsTable() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fundName": "Фонд Допомоги",
                                  "edrpou": "11112222",
                                  "bankDetail": "UA213223130000026007233566001",
                                  "registeredAddress": "м. Київ, вул. Хрещатик, 1",
                                  "phoneNumber": "+380501234567",
                                  "corpEmail": "contact@fund-help.org",
                                  "website": "https://fund-help.org",
                                  "socialMediaUrls": {"facebook": "https://facebook.com/fundhelp"}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(URL + "/1")))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));

        Map<String, Object> row = jdbc.sql("""
                        select fund_name, actual_address, status, social_media_urls::text as social
                        from funds where edrpou = ?""")
                .param("11112222")
                .query()
                .singleRow();
        assertThat(row.get("fund_name")).isEqualTo("Фонд Допомоги");
        assertThat(row.get("actual_address")).isEqualTo("м. Київ, вул. Хрещатик, 1");
        assertThat(row.get("status")).isEqualTo("PENDING_APPROVAL");
        assertThat((String) row.get("social")).contains("https://facebook.com/fundhelp");
    }

    @Test
    void createFund_WithDuplicateEdrpou_ShouldReturn409AndKeepSingleRow() throws Exception {
        insertFund("33334444", "PENDING_APPROVAL");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fundName": "Дублікат",
                                  "edrpou": "33334444",
                                  "bankDetail": "UA00",
                                  "registeredAddress": "Львів",
                                  "phoneNumber": "+380501234567",
                                  "corpEmail": "dup@fund.org",
                                  "website": "https://dup.org"
                                }
                                """))
                .andExpect(status().isConflict());

        assertThat(countFunds()).isEqualTo(1);
    }

    @Test
    void getFundById_ShouldReturnFundFromTable() throws Exception {
        long id = insertFund("55556666", "APPROVED");

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.edrpou").value("55556666"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getFundById_WhenMissing_ShouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllFunds_ShouldReturnEveryRow() throws Exception {
        insertFund("10000001", "PENDING_APPROVAL");
        insertFund("10000002", "APPROVED");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void updateFund_ShouldChangeRowInTable() throws Exception {
        long id = insertFund("20000001", "PENDING_APPROVAL");

        mockMvc.perform(put(URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fundName": "Нова назва",
                                  "registeredAddress": "Одеса",
                                  "actualAddress": "Дніпро",
                                  "phoneNumber": "+380671112233",
                                  "corpEmail": "new@fund.org",
                                  "website": "https://new-fund.org"
                                }
                                """))
                .andExpect(status().isOk());

        Map<String, Object> row = jdbc.sql("""
                        select fund_name, registered_address, actual_address, corp_email
                        from funds where id = ?""")
                .param(id)
                .query()
                .singleRow();
        assertThat(row.get("fund_name")).isEqualTo("Нова назва");
        assertThat(row.get("registered_address")).isEqualTo("Одеса");
        assertThat(row.get("actual_address")).isEqualTo("Дніпро");
        assertThat(row.get("corp_email")).isEqualTo("new@fund.org");
    }

    @Test
    void updateFundStatus_ShouldChangeStatusAndSaveVerificationAct() throws Exception {
        long fundId = insertFund("30000001", "PENDING_APPROVAL");
        long adminId = insertSystemAdmin();

        mockMvc.perform(patch(URL + "/{id}/status", fundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED", "adminId": %d, "comment": "Документи перевірено"}
                                """.formatted(adminId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        assertThat(jdbc.sql("select status from funds where id = ?")
                .param(fundId)
                .query(String.class)
                .single()).isEqualTo("APPROVED");
        Map<String, Object> act = jdbc.sql("""
                        select system_admin_id, decision, comment
                        from verification_acts where fund_id = ?""")
                .param(fundId)
                .query()
                .singleRow();
        assertThat(act.get("system_admin_id")).isEqualTo(adminId);
        assertThat(act.get("decision")).isEqualTo("APPROVED");
        assertThat(act.get("comment")).isEqualTo("Документи перевірено");
    }

    @Test
    void updateFundDescription_ForApprovedFund_ShouldSaveDescription() throws Exception {
        long id = insertFund("40000001", "APPROVED");

        mockMvc.perform(patch(URL + "/{id}/description", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Допомагаємо дітям"}
                                """))
                .andExpect(status().isOk());

        assertThat(jdbc.sql("select description from funds where id = ?")
                .param(id)
                .query(String.class)
                .single()).isEqualTo("Допомагаємо дітям");
    }

    @Test
    void updateFundDescription_ForPendingFund_ShouldReturn409AndKeepRow() throws Exception {
        long id = insertFund("40000002", "PENDING_APPROVAL");

        mockMvc.perform(patch(URL + "/{id}/description", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Допомагаємо дітям"}
                                """))
                .andExpect(status().isConflict());

        assertThat(jdbc.sql("select description from funds where id = ?")
                .param(id)
                .query(String.class)
                .optional()).isEmpty();
    }

    @Test
    void deleteFund_ShouldRemoveRowFromTable() throws Exception {
        long id = insertFund("50000001", "PENDING_APPROVAL");

        mockMvc.perform(delete(URL + "/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(countFunds()).isZero();
    }

    private long insertFund(String edrpou, String status) {
        return jdbc.sql("""
                        insert into funds (fund_name, edrpou, bank_detail, registered_address, actual_address,
                                           phone_number, corp_email, website, social_media_urls, status)
                        values (?, ?, 'UA00', 'Київ', 'Київ', '+380501112233', 'fund@test.org',
                                'https://fund.test', '{}', ?)
                        returning id""")
                .params("Фонд " + edrpou, edrpou, status)
                .query(Long.class)
                .single();
    }

    private long insertSystemAdmin() {
        return jdbc.sql("""
                        insert into system_admins (email, password_hash, first_name, last_name)
                        values ('admin@fund.test', 'hash', 'Test', 'Admin')
                        returning id""")
                .query(Long.class)
                .single();
    }

    private long countFunds() {
        return jdbc.sql("select count(*) from funds").query(Long.class).single();
    }
}
