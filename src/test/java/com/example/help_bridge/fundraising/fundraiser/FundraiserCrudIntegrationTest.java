package com.example.help_bridge.fundraising.fundraiser;

import com.example.help_bridge.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@WithMockUser(roles = "SYSTEM_ADMIN")
class FundraiserCrudIntegrationTest {

    private static final String URL = "/api/fundraiser";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void getFundraiserById_ShouldReturnRowWithEvidences() throws Exception {
        long id = insertFundraiser(42L, "IN_PROGRESS");
        insertEvidence(id, "R-001");

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fundraiserId").value(id))
                .andExpect(jsonPath("$.requestId").value(42))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.evidences.size()").value(1))
                .andExpect(jsonPath("$.evidences[0].receiptNumber").value("R-001"));
    }

    @Test
    void getFundraiserById_WhenMissing_ShouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllFundraisers_ShouldReturnEveryRow() throws Exception {
        insertFundraiser(1L, "PENDING_ASSIGNMENT");
        insertFundraiser(2L, "IN_PROGRESS");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void closeFundraiser_WithEvidence_ShouldSetClosedStatusInTable() throws Exception {
        long id = insertFundraiser(7L, "IN_PROGRESS");
        insertEvidence(id, "R-007");

        mockMvc.perform(post(URL + "/{id}/close", id))
                .andExpect(status().isNoContent());

        Map<String, Object> row = jdbc.sql("select status, closed_at from fundraiser where id = ?")
                .param(id)
                .query()
                .singleRow();
        assertThat(row.get("status")).isEqualTo("CLOSED");
        assertThat(row.get("closed_at")).isNotNull();
    }

    @Test
    void closeFundraiser_WithoutEvidence_ShouldReturn409AndKeepStatus() throws Exception {
        long id = insertFundraiser(8L, "IN_PROGRESS");

        mockMvc.perform(post(URL + "/{id}/close", id))
                .andExpect(status().isConflict());

        assertThat(statusOf(id)).isEqualTo("IN_PROGRESS");
    }

    @Test
    void closeFundraiser_WhenAlreadyClosed_ShouldReturn409() throws Exception {
        long id = insertFundraiser(9L, "CLOSED");
        insertEvidence(id, "R-009");

        mockMvc.perform(post(URL + "/{id}/close", id))
                .andExpect(status().isConflict());

        assertThat(statusOf(id)).isEqualTo("CLOSED");
    }

    private long insertFundraiser(Long requestId, String status) {
        return jdbc.sql("""
                        insert into fundraiser (request_id, status, created_at)
                        values (?, ?, now())
                        returning id""")
                .params(requestId, status)
                .query(Long.class)
                .single();
    }

    private void insertEvidence(long fundraiserId, String receiptNumber) {
        jdbc.sql("""
                        insert into evidences (fundraiser_id, receipt_number, created_at)
                        values (?, ?, now())""")
                .params(fundraiserId, receiptNumber)
                .update();
    }

    private String statusOf(long id) {
        return jdbc.sql("select status from fundraiser where id = ?")
                .param(id)
                .query(String.class)
                .single();
    }
}
