package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DataSourceConfig {
    public static DataSource getDataSource() {
        String jdbcUrl = System.getenv("JDBC_DATABASE_URL");
        HikariConfig config = new HikariConfig();

        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl("jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;");
            config.setUsername("sa");
            config.setPassword("");
        } else {
            config.setJdbcUrl(jdbcUrl);
        }
        return new HikariDataSource(config);
    }

}
