package com.example.help_bridge.fundraising.fundraiser.migration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentCommentsMigrationTest {
    @Test void migrationPreservesOldRowsAndCanBeRunTwice() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:assignment_migration;MODE=PostgreSQL");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE fundraiser_assignment (id BIGINT PRIMARY KEY, return_reason VARCHAR(255))");
            statement.execute("INSERT INTO fundraiser_assignment VALUES (1, 'Old reason')");
            var script = new ClassPathResource("db/manual-migrations/20261113_assignment_comments.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var rows = statement.executeQuery("SELECT return_reason, closing_comment FROM fundraiser_assignment WHERE id=1")) {
                assertTrue(rows.next());
                assertEquals("Old reason", rows.getString(1));
                assertNull(rows.getString(2));
            }
            try (var update = connection.prepareStatement("UPDATE fundraiser_assignment SET return_reason=?, closing_comment=? WHERE id=1")) {
                update.setString(1, "р".repeat(500));
                update.setString(2, "к".repeat(500));
                assertEquals(1, update.executeUpdate());
            }
        }
    }
}
