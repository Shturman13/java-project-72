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

    /**
     * Saves the URL to the database.
     * This method is not intended for overriding; for custom save logic,
     * extend this class and use composition to modify behavior safely.
     * @param url the URL to save
     * @return the saved URL with generated ID
     * @throws SQLException if a database error occurs
     */
    public Url save(Url url) throws SQLException {
        String sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
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
                        log.info("Saved URL with id: {}", url.getId());
                    } else {
                        log.warn("No generated key returned for URL: {}", url.getName());
                    }
                }
            } else {
                log.warn("No rows affected while saving URL: {}", url.getName());
            }
            return url;
        } catch (SQLException e) {
            log.error("Database error saving URL: {}, error: {}", url.getName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Finds all URLs in the database.
     * This method is not intended for overriding; for custom query logic,
     * extend this class and use composition to modify behavior safely.
     * @return a list of all URLs
     * @throws SQLException if a database error occurs
     */
    public List<Url> findAll() throws SQLException {
        String sql = "SELECT id, name, created_at FROM urls";
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

    /**
     * Finds a URL by its ID.
     * This method is not intended for overriding; for custom query logic,
     * extend this class and use composition to modify behavior safely.
     * @param id the ID of the URL
     * @return an optional containing the URL, or empty if not found
     * @throws SQLException if a database error occurs
     */
    public Optional<Url> findById(Long id) throws SQLException {
        String sql = "SELECT id, name, created_at FROM urls WHERE id = ?";
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

    /**
     * Finds a URL by its name.
     * This method is not intended for overriding; for custom query logic,
     * extend this class and use composition to modify behavior safely.
     * @param name the name of the URL
     * @return an optional containing the URL, or empty if not found
     * @throws SQLException if a database error occurs
     */
    public Optional<Url> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, created_at FROM urls WHERE name = ?";
        log.info("Searching for URL with name: '{}'", name);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Url url = new Url(rs.getString("name"), rs.getTimestamp("created_at"));
                    url.setId(rs.getLong("id"));
                    log.info("Found URL by name '{}': {}", name, url);
                    return Optional.of(url);
                } else {
                    log.warn("No URL found for name: '{}'. Checking table contents...", name);
                    List<Url> allUrls = findAll();
                    log.warn("Current table contents: {}", allUrls);
                    return Optional.empty();
                }
            }
        }
    }
}
