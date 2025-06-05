package hexlet.code;


import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DatabaseInitializer {
    public static void initialize(DataSource dataSource) {
        String sql = """
            CREATE TABLE IF NOT EXISTS page_analyzer (
                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                created_at TIMESTAMP NOT NULL
            )
            """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("Database initialized successfully");
        } catch (SQLException e) {
            log.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}