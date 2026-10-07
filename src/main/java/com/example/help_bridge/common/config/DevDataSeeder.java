package com.example.help_bridge.common.config;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final EntityManager entityManager;

    public DevDataSeeder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Number count = (Number) entityManager
                .createNativeQuery("select count(*) from users")
                .getSingleResult();

        if (count.longValue() == 0) {
            entityManager.createNativeQuery(
                            "insert into users (first_name, last_name, email) values (?1, ?2, ?3)")
                    .setParameter(1, "Test")
                    .setParameter(2, "Requester")
                    .setParameter(3, "requester@helpbridge.test")
                    .executeUpdate();
            log.info("Dev profile: demo user inserted (id=1)");
        }
    }
}