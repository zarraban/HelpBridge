package com.example.help_bridge.users.systemadmin.repository;

import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SystemAdminRepositoryTest {

    @Autowired
    private SystemAdminRepository repository;

    @Test
    void existsByEmail_andFindByEmail_work() {
        repository.saveAndFlush(new SystemAdmin("admin@help.ua", "hash", "Daria", "Chorna"));

        assertThat(repository.existsByEmail("admin@help.ua")).isTrue();
        assertThat(repository.existsByEmail("other@help.ua")).isFalse();
        assertThat(repository.findByEmail("admin@help.ua")).isPresent();
        assertThat(repository.findByEmail("other@help.ua")).isEmpty();
    }

    @Test
    void save_duplicateEmail_violatesUniqueConstraint() {
        repository.saveAndFlush(new SystemAdmin("admin@help.ua", "hash", "Daria", "Chorna"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new SystemAdmin("admin@help.ua", "hash2", "Olena", "Shevchenko")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void searchByLastName_matchesPartIgnoringCase() {
        repository.saveAndFlush(new SystemAdmin("a@help.ua", "hash", "Daria", "Chorna"));
        repository.saveAndFlush(new SystemAdmin("b@help.ua", "hash", "Olena", "Shevchenko"));

        assertThat(repository.searchByLastName("CHOR"))
                .extracting(SystemAdmin::getEmail)
                .containsExactly("a@help.ua");
        assertThat(repository.searchByLastName("xyz")).isEmpty();
    }

}