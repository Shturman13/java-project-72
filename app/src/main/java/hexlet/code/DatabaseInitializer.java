package hexlet.code;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    public static void initialize(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = Files.readString(Paths.get("database.sql"));
            stmt.execute(sql);
        } catch (Exception e) {
            throw new SQLException("Failed to initialize database", e);
        }
    }
}

