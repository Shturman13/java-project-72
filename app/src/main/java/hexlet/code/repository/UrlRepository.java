package hexlet.code.repository;

import hexlet.code.model.Url;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UrlRepository {
    private final DataSource dataSource;

    public UrlRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Url url) throws SQLException {
        String sql = "INSERT INTO page_analyzer (name, created_at) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, url.getName());
            stmt.setTimestamp(2, url.getCreatedAt());
            int rows = stmt.executeUpdate();
            log.info("Rows affected by save: {}", rows);
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    url.setId(generatedId);
                    log.info("Generated ID for URL {}: {}", url.getName(), generatedId);
                } else {
                    log.warn("No generated ID returned for URL: {}", url.getName());
                }
            }
        }
    }

    public List<Url> findAll() {
        String sql = "SELECT id, name, created_at FROM page_analyzer";
        List<Url> urls = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Url url = new Url(rs.getString("name"), rs.getTimestamp("created_at"));
                url.setId(rs.getLong("id"));
                urls.add(url);
            }
            log.info("Found {} URLs", urls.size());
        } catch (SQLException e) {
            log.error("Error retrieving URLs", e);
        }
        return urls;
    }

    public Optional<Url> findById(Long id) {
        String sql = "SELECT id, name, created_at FROM page_analyzer WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Url url = new Url(rs.getString("name"), rs.getTimestamp("created_at"));
                url.setId(rs.getLong("id"));
                log.info("Found URL by ID {}: {}", id, url);
                return Optional.of(url);
            } else {
                log.warn("No URL found for ID: {}", id);
            }
        } catch (SQLException e) {
            log.error("Error finding URL by ID: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<Url> findByName(String name) {
        String sql = "SELECT id, name, created_at FROM page_analyzer WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Url url = new Url(rs.getString("name"), rs.getTimestamp("created_at"));
                url.setId(rs.getLong("id"));
                log.info("Found URL by name {}: {}", name, url);
                return Optional.of(url);
            }
        } catch (SQLException e) {
            log.error("Error finding URL by name: {}", name, e);
        }
        return Optional.empty();
    }
}
