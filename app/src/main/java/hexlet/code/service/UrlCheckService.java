// src/main/java/hexlet/code/service/UrlCheckService.java
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

public class UrlCheckService {
    private final UrlCheckRepository urlCheckRepository;

    public UrlCheckService(UrlCheckRepository urlCheckRepository) {
        this.urlCheckRepository = urlCheckRepository;
    }

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
            String h1 = doc.selectFirst("h1") != null ? doc.selectFirst("h1").text() : null;
            String description = doc.selectFirst("meta[name=description]") != null
                    ? doc.selectFirst("meta[name=description]").attr("content") : null;

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
            check.setStatusCode(0); // Индикатор ошибки
            check.setCreatedAt(Timestamp.from(Instant.now()));
            urlCheckRepository.save(check);
            return check;
        }
    }
}
