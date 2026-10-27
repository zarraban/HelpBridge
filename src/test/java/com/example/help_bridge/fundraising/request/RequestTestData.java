package com.example.help_bridge.fundraising.request;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

final class RequestTestData {

    private final JdbcClient jdbc;

    RequestTestData(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    long insertUser(String email) {
        return jdbc.sql("""
                        insert into users (email, password_hash, first_name, last_name)
                        values (?, 'hash', 'Test', 'Applicant')
                        returning id""")
                .param(email)
                .query(Long.class)
                .single();
    }

    long insertFund(String edrpou, String status) {
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

    long insertSystemAdmin() {
        return jdbc.sql("""
                        insert into system_admins (email, password_hash, first_name, last_name)
                        values ('admin@request.test', 'hash', 'Test', 'Admin')
                        returning id""")
                .query(Long.class)
                .single();
    }

    long insertRequest(long userId, String status) {
        return insertRequest(userId, status, null);
    }

    long insertRequest(long userId, String status, Long fundId) {
        return jdbc.sql("""
                        insert into requests (version, assistance_type_code, amount, deadline,
                                              situation_description, need_description, institution_name,
                                              application_number, data_processing_consent, created_at,
                                              status, user_id, fund_id)
                        values (0, 'MEDICAL', ?, ?, 'Ситуація', 'Потреба', 'Лікарня №1',
                                'APP-TEST-1', true, ?, ?, ?, ?)
                        returning id""")
                .params(new BigDecimal("15000.00"), LocalDate.now().plusMonths(3),
                        LocalDateTime.now(), status, userId, fundId)
                .query(Long.class)
                .single();
    }

    long insertDocument(long requestId, String fileName, String fileUrl) {
        return jdbc.sql("""
                        insert into request_documents (file_name, file_url, request_id)
                        values (?, ?, ?)
                        returning id""")
                .params(fileName, fileUrl, requestId)
                .query(Long.class)
                .single();
    }

    long count(String table) {
        return jdbc.sql("select count(*) from " + table).query(Long.class).single();
    }

    String requestStatus(long requestId) {
        return jdbc.sql("select status from requests where id = ?")
                .param(requestId)
                .query(String.class)
                .single();
    }

    Long requestFundId(long requestId) {
        return jdbc.sql("select fund_id from requests where id = ?")
                .param(requestId)
                .query(Long.class)
                .optional()
                .orElse(null);
    }
}