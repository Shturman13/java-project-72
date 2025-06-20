package hexlet.code.service;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

public class UrlCheckService {
    private final UrlCheckRepository urlCheckRepository;

    public UrlCheckService(UrlCheckRepository urlCheckRepository) {
        this.urlCheckRepository = urlCheckRepository;
    }

    /**
     * Checks the URL and saves the result to the database.
     * This method is not intended for overriding; for custom check logic,
     * extend this class and use composition to modify behavior safely.
     * @param url the URL to check
     * @return the saved URL check
     * @throws SQLException if a database error occurs
     */
    public UrlCheck checkUrl(Url url) throws SQLException {
        try {
            HttpResponse<String> response = Unirest.get(url.getName())
                    .connectTimeout(2000)
                    .socketTimeout(2000)
                    .asString();
            int statusCode = response.getStatus();
            String body = response.getBody();

            Document doc = Jsoup.parse(body);
            String title = doc.title().isEmpty() ? null : doc.title();
            String h1 = doc.selectFirst("h1") != null ? Objects.requireNonNull(doc.selectFirst("h1")).text() : null;
            String description = doc.selectFirst("meta[name=description]") != null
                    ? Objects.requireNonNull(doc.selectFirst("meta[name=description]")).attr("content") : null;

            UrlCheck check = new UrlCheck();
            check.setUrlId(url.getId());
            check.setStatusCode(statusCode);
            check.setTitle(title);
            check.setH1(h1);
            check.setDescription(description);
            check.setCreatedAt(Timestamp.from(Instant.now()));

            urlCheckRepository.save(check);
            return check;

        } catch (Exception e) {
            UrlCheck check = new UrlCheck();
            check.setUrlId(url.getId());
            check.setStatusCode(0);
            check.setCreatedAt(Timestamp.from(Instant.now()));
            urlCheckRepository.save(check);
            return check;
        }
    }
}
