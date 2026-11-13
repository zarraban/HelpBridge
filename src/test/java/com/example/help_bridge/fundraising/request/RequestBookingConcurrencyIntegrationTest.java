package com.example.help_bridge.fundraising.request;

import com.example.help_bridge.fundraising.request.event.RequestBookedEvent;
import com.example.help_bridge.support.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@IntegrationTest
@Import(RequestBookingConcurrencyIntegrationTest.BarrierConfiguration.class)
class RequestBookingConcurrencyIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcClient jdbc;
    @Autowired BookingBarrier barrier;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void concurrentBookingsCommitOnceAndRollbackLosingFundraiser(boolean sameFund) throws Exception {
        var data = new RequestTestData(jdbc);
        long owner = data.insertUser("concurrent@applicant.test");
        long first = data.insertFund("91000001", "APPROVED");
        long second = sameFund ? first : data.insertFund("91000002", "APPROVED");
        representative("first@concurrent.test", first);
        representative("second@concurrent.test", second);
        long requestId = data.insertRequest(owner, "NEW");
        barrier.rendezvous = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var a = executor.submit(() -> book(requestId, "first@concurrent.test"));
            var b = executor.submit(() -> book(requestId, "second@concurrent.test"));
            var results = List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
            assertThat(results).allSatisfy(result -> assertThat(result.getResponse().getStatus()).isEqualTo(302));
            assertThat(results.stream().map(result -> result.getResponse().getRedirectedUrl()).toList())
                    .containsExactlyInAnyOrder("/requests/" + requestId, "/requests");
            var loser = results.stream().filter(result -> "/requests".equals(result.getResponse().getRedirectedUrl())).findFirst().orElseThrow();
            assertThat((java.util.Map<String, Object>) loser.getFlashMap()).containsKey("error").doesNotContainKey("message");
            assertThat(data.requestStatus(requestId)).isEqualTo("IN_PROGRESS");
            long winningFund = data.requestFundId(requestId);
            long expectedFund = results.getFirst().getResponse().getRedirectedUrl().equals("/requests") ? second : first;
            assertThat(winningFund).isEqualTo(expectedFund);
            assertThat(jdbc.sql("select count(*) from fundraiser where request_id = ?").param(requestId).query(Long.class).single()).isEqualTo(1);
            assertThat(jdbc.sql("select version from requests where id = ?").param(requestId).query(Long.class).single()).isEqualTo(1);
        } finally {
            barrier.rendezvous = null;
        }
    }

    private MvcResult book(long id, String email) throws Exception {
        return mvc.perform(post("/requests/{id}/book", id)
                .with(user("FUND_REPRESENTATIVE:" + email).roles("FUND_REPRESENTATIVE")).with(csrf())).andReturn();
    }

    private void representative(String email, long fund) {
        jdbc.sql("""
                insert into fund_representatives (first_name, last_name, email, phone, password_hash, fund_id)
                values ('Test', 'Rep', ?, '+380501112233', 'hash', ?)
                """).params(email, fund).update();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class BarrierConfiguration {
        @Bean BookingBarrier bookingBarrier() { return new BookingBarrier(); }
    }

    static class BookingBarrier {
        volatile CyclicBarrier rendezvous;

        // Both transactions must have read NEW and reached the booking event before either can commit.
        @EventListener
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public void awaitBothBookings(RequestBookedEvent event) throws Exception {
            var active = rendezvous;
            if (active != null) active.await(10, TimeUnit.SECONDS);
        }
    }
}
