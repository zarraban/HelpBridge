package com.example.help_bridge.notification.listener;

import com.example.help_bridge.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@WithMockUser(username = "FUND_REPRESENTATIVE:rep@mail.test", roles = "FUND_REPRESENTATIVE")
class NotificationEventListenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void sendMail_ShouldCreateMailingTaskForEveryDonorAsynchronously() throws Exception {
        long fundraiserId = insertFundraiser();
        insertDonor(fundraiserId, "olena@donor.test");
        insertDonor(fundraiserId, "petro@donor.test");

        sendMail(fundraiserId);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Map<String, Object> task = jdbc.sql("""
                            select subject, message_body, status
                            from mailing_task where fundraiser_id = ?""")
                    .param(fundraiserId)
                    .query()
                    .singleRow();
            assertThat(task.get("subject")).isEqualTo("Звіт про збір");
            assertThat(task.get("message_body")).isEqualTo("Дякуємо за підтримку!");
            assertThat(task.get("status")).isEqualTo("PENDING");

            assertThat(jdbc.sql("""
                            select r.email from mailing_recipient r
                            join mailing_task t on t.id = r.task_id
                            where t.fundraiser_id = ? and r.status = 'PENDING'""")
                    .param(fundraiserId)
                    .query(String.class)
                    .list()).containsExactlyInAnyOrder("olena@donor.test", "petro@donor.test");
        });
    }

    @Test
    void sendMail_WithoutDonors_ShouldCreateTaskWithoutRecipients() throws Exception {
        long fundraiserId = insertFundraiser();

        sendMail(fundraiserId);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(jdbc.sql("select count(*) from mailing_task where fundraiser_id = ?")
                        .param(fundraiserId)
                        .query(Long.class)
                        .single()).isEqualTo(1));
        assertThat(jdbc.sql("select count(*) from mailing_recipient")
                .query(Long.class)
                .single()).isZero();
    }

    private void sendMail(long fundraiserId) throws Exception {
        mockMvc.perform(post("/api/fundraiser/mail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fundraiserId": %d, "subjectOfMail": "Звіт про збір", "message": "Дякуємо за підтримку!"}
                                """.formatted(fundraiserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fundraiserId").value(fundraiserId));
    }

    private long insertFundraiser() {
        long fundId = jdbc.sql("""
                insert into funds (fund_name, edrpou, bank_detail, registered_address, actual_address,
                                   phone_number, corp_email, website, social_media_urls, status)
                values ('Mail Fund', '87654321', 'UA00', 'Kyiv', 'Kyiv', '+380501112233',
                        'fund@mail.test', 'https://fund.test', '{}', 'APPROVED') returning id
                """).query(Long.class).single();
        jdbc.sql("""
                insert into fund_representatives (first_name, last_name, email, phone, password_hash, fund_id)
                values ('Test', 'Representative', 'rep@mail.test', '+380501112234', 'hash', ?)
                """).param(fundId).update();
        long userId = jdbc.sql("""
                insert into users (email, password_hash, first_name, last_name)
                values ('applicant@mail.test', 'hash', 'Test', 'Applicant') returning id
                """).query(Long.class).single();
        long requestId = jdbc.sql("""
                insert into requests (version, assistance_type_code, amount, deadline, situation_description,
                                      need_description, institution_name, application_number,
                                      data_processing_consent, created_at, status, user_id, fund_id)
                values (0, 'MEDICAL', 100, CURRENT_DATE + 30, 'Situation', 'Need', 'Hospital', 'MAIL-1',
                        true, now(), 'IN_PROGRESS', ?, ?) returning id
                """).params(userId, fundId).query(Long.class).single();
        return jdbc.sql("""
                        insert into fundraiser (request_id, status, created_at)
                        values (?, 'IN_PROGRESS', now())
                        returning id""")
                .param(requestId)
                .query(Long.class)
                .single();
    }

    private void insertDonor(long fundraiserId, String email) {
        jdbc.sql("""
                        insert into donors (fundraiser_id, first_name, last_name, email, created_at)
                        values (?, 'Test', 'Donor', ?, now())""")
                .params(fundraiserId, email)
                .update();
    }
}
