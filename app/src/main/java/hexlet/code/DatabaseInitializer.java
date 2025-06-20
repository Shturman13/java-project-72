package hexlet.code;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    public static void initialize(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            InputStream sqlStream = DatabaseInitializer.class.getClassLoader()
                    .getResourceAsStream("database.sql");
            if (sqlStream == null) {
                throw new SQLException("Resource database.sql not found in classpath");
            }
            String sql = new String(sqlStream.readAllBytes());
            stmt.execute(sql);
        } catch (Exception e) {
            throw new SQLException("Failed to initialize database", e);
        }
    }
}

