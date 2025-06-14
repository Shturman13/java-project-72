package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlCheckRepository {
    private final DataSource dataSource;

    public UrlCheckRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(UrlCheck urlCheck) throws SQLException {
        String sql = "INSERT INTO url_checks (url_id, status_code, title, h1, description, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, urlCheck.getUrlId());
            stmt.setInt(2, urlCheck.getStatusCode());
            stmt.setString(3, urlCheck.getTitle());
            stmt.setString(4, urlCheck.getH1());
            stmt.setString(5, urlCheck.getDescription());
            stmt.setTimestamp(6, urlCheck.getCreatedAt());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    urlCheck.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public List<UrlCheck> findByUrlId(Long urlId) throws SQLException {
        String sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC";
        List<UrlCheck> checks = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UrlCheck check = new UrlCheck();
                    check.setId(rs.getLong("id"));
                    check.setUrlId(rs.getLong("url_id"));
                    check.setStatusCode(rs.getInt("status_code"));
                    check.setTitle(rs.getString("title"));
                    check.setH1(rs.getString("h1"));
                    check.setDescription(rs.getString("description"));
                    check.setCreatedAt(rs.getTimestamp("created_at"));
                    checks.add(check);
                }
            }
        }
        return checks;
    }

    public Optional<UrlCheck> findLastCheckByUrlId(Long urlId) throws SQLException {
        String sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UrlCheck check = new UrlCheck();
                    check.setId(rs.getLong("id"));
                    check.setUrlId(rs.getLong("url_id"));
                    check.setStatusCode(rs.getInt("status_code"));
                    check.setTitle(rs.getString("title"));
                    check.setH1(rs.getString("h1"));
                    check.setDescription(rs.getString("description"));
                    check.setCreatedAt(rs.getTimestamp("created_at"));
                    return Optional.of(check);
                }
            }
        }
        return Optional.empty();
    }
}
