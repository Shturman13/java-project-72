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

    public Url save(Url url) throws SQLException {
        String sql = "INSERT INTO page_analyzer (name, created_at) VALUES (?, ?)"; // Удаляем RETURNING id
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, url.getName());
            stmt.setTimestamp(2, url.getCreatedAt());
            int rows = stmt.executeUpdate();
            log.info("Rows affected by save: {}", rows);
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        url.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Database error saving URL: {}", url.getName(), e);
            throw e;
        }
        return url; // Возвращаем объект с установленным id
    }

    public List<Url> findAll() throws SQLException {
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
            return urls;
        }
    }

    public Optional<Url> findById(Long id) throws SQLException {
        String sql = "SELECT id, name, created_at FROM page_analyzer WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Url url = new Url(rs.getString("name"), rs.getTimestamp("created_at"));
                    url.setId(rs.getLong("id"));
                    return Optional.of(url);
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Url> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, created_at FROM page_analyzer WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Url url = new Url(rs.getString("name"), rs.getTimestamp("created_at"));
                    url.setId(rs.getLong("id"));
                    return Optional.of(url);
                }
                return Optional.empty();
            }
        }
    }
}
