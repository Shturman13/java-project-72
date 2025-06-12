package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlRepository;
import hexlet.code.repository.UrlCheckRepository;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class UrlsController {
    private static UrlRepository urlRepository;
    private static UrlCheckRepository urlCheckRepository;

    // Инициализация репозиториев
    public static void init(UrlRepository urlRepo, UrlCheckRepository checkRepo) {
        urlRepository = urlRepo;
        urlCheckRepository = checkRepo;
    }

    public static void index(Context ctx) {
        String flash = ctx.consumeSessionAttribute("flash");
        String flashType = ctx.consumeSessionAttribute("flashType");
        log.info("GET /: Consumed flash: {}, flashType: {}", flash, flashType);
        ctx.render("index.jte", Map.of(
                "flash", flash != null ? flash : "",
                "flashType", flashType != null ? flashType : "info"
        ));
    }

    public static void create(Context ctx) {
        String inputUrl = ctx.formParam("url");
        log.info("Received URL: {}", inputUrl);
        if (inputUrl == null || inputUrl.isBlank()) {
            log.warn("Invalid URL: null or blank");
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flashType", "danger");
            log.info("POST /urls: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }
        String normalizedUrl;
        try {
            URL url = URI.create(inputUrl).toURL();
            StringBuilder sb = new StringBuilder();
            sb.append(url.getProtocol()).append("://").append(url.getHost());
            if (url.getPort() != -1) {
                sb.append(":").append(url.getPort());
            }
            normalizedUrl = sb.toString().toLowerCase();
            log.info("Normalized URL: {}", normalizedUrl);
        } catch (MalformedURLException | IllegalArgumentException e) {
            log.warn("URL parsing error for input: {}. Reason: {}", inputUrl, e.getMessage());
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flashType", "danger");
            log.info("POST /urls: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        try {
            Optional<Url> existingUrl = urlRepository.findByName(normalizedUrl);
            if (existingUrl.isPresent()) {
                log.info("URL already exists: {}", normalizedUrl);
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flashType", "info");
                log.info("POST /urls: Set flash: {}, flashType: {}",
                        ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }

            Url url = new Url(normalizedUrl, Timestamp.from(Instant.now()));
            urlRepository.save(url);
            log.info("URL saved: {}", normalizedUrl);
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flashType", "success");
            log.info("POST /urls: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (SQLException e) {
            log.error("Database error saving URL: {}", normalizedUrl, e);
            ctx.sessionAttribute("flash", "Ошибка при добавлении URL");
            ctx.sessionAttribute("flashType", "danger");
            log.info("POST /urls: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.rootPath());
        }
    }

    public static void list(Context ctx) {
        try {
            List<Url> urls = urlRepository.findAll();
            Map<Long, UrlCheck> lastChecks = new HashMap<>();
            for (Url url : urls) {
                try {
                    urlCheckRepository.findLastCheckByUrlId(url.getId())
                            .ifPresent(check -> lastChecks.put(url.getId(), check));
                } catch (SQLException e) {
                    log.error("Error retrieving last check for URL {}: {}", url.getId(), e);
                }
            }
            log.info("URLs retrieved: {}, content: {}", urls.size(), urls);
            String flash = ctx.consumeSessionAttribute("flash");
            String flashType = ctx.consumeSessionAttribute("flashType");
            log.info("GET /urls: Consumed flash: {}, flashType: {}", flash, flashType);
            ctx.render("urls.jte", Map.of(
                    "urls", urls,
                    "lastChecks", lastChecks,
                    "flash", flash != null ? flash : "",
                    "flashType", flashType != null ? flashType : "info"
            ));
        } catch (SQLException e) {
            log.error("Error retrieving URLs: {}", e.getMessage());
            ctx.sessionAttribute("flash", "Ошибка при получении списка URL");
            ctx.sessionAttribute("flashType", "danger");
            log.info("GET /urls: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.rootPath());
        }
    }

    public static void show(Context ctx) {
        Long id;
        try {
            id = ctx.pathParamAsClass("id", Long.class).get();
        } catch (Exception e) {
            log.error("Invalid ID format: {}", ctx.pathParam("id"), e);
            ctx.sessionAttribute("flash", "Неверный ID");
            ctx.sessionAttribute("flashType", "danger");
            log.info("GET /urls/{id}: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.urlsPath());
            return;
        }
        Optional<Url> url;
        try {
            url = urlRepository.findById(id);
            if (url.isEmpty()) {
                log.warn("URL not found for id: {}", id);
                ctx.sessionAttribute("flash", "URL не найден");
                ctx.sessionAttribute("flashType", "danger");
                log.info("GET /urls/{id}: Set flash: {}, flashType: {}",
                        ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }
        } catch (SQLException e) {
            log.error("Database error finding URL by id: {}", id, e);
            ctx.sessionAttribute("flash", "Ошибка при получении URL");
            ctx.sessionAttribute("flashType", "danger");
            log.info("GET /urls/{id}: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.urlsPath());
            return;
        }

        List<UrlCheck> checks;
        try {
            checks = urlCheckRepository.findByUrlId(id);
        } catch (SQLException e) {
            log.error("Error retrieving checks for URL {}: {}", id, e);
            checks = Collections.emptyList();
        }
        String flash = ctx.consumeSessionAttribute("flash");
        String flashType = ctx.consumeSessionAttribute("flashType");
        log.info("GET /urls/{}: Consumed flash: {}, flashType: {}", id, flash, flashType);
        ctx.render("urls/show.jte", Map.of(
                "url", url.get(),
                "checks", checks,
                "flash", flash != null ? flash : "",
                "flashType", flashType != null ? flashType : "info"
        ));
    }

    public static void check(Context ctx) throws SQLException {
        Long id;
        try {
            id = ctx.pathParamAsClass("id", Long.class).get();
        } catch (Exception e) {
            log.error("Invalid ID format: {}", ctx.pathParam("id"), e);
            ctx.sessionAttribute("flash", "Неверный ID");
            ctx.sessionAttribute("flashType", "danger");
            log.info("POST /urls/{id}/checks: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.urlsPath());
            return;
        }

        Optional<Url> url;
        try {
            url = urlRepository.findById(id);
            if (url.isEmpty()) {
                log.warn("URL not found for id: {}", id);
                ctx.sessionAttribute("flash", "URL не найден");
                ctx.sessionAttribute("flashType", "danger");
                log.info("POST /urls/{id}/checks: Set flash: {}, flashType: {}",
                        ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }
        } catch (SQLException e) {
            log.error("Database error finding URL by id: {}", id, e);
            ctx.sessionAttribute("flash", "Ошибка при получении URL");
            ctx.sessionAttribute("flashType", "danger");
            log.info("POST /urls/{id}/checks: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
            ctx.redirect(NamedRoutes.urlsPath());
            return;
        }

        try {
            URLConnection conn = new URL(url.get().getName()).openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setConnectTimeout(2000);
            httpConn.setReadTimeout(2000);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            int statusCode = httpConn.getResponseCode();
            Document doc = Jsoup.parse(httpConn.getInputStream(), "UTF-8", url.get().getName());
            String title = doc.title();
            String h1 = doc.selectFirst("h1") != null ? doc.selectFirst("h1").text() : "";
            String description = doc.selectFirst("meta[name=description]") != null
                    ? doc.selectFirst("meta[name=description]").attr("content") : "";

            UrlCheck check = new UrlCheck();
            check.setUrlId(id);
            check.setStatusCode(statusCode);
            check.setTitle(title);
            check.setH1(h1);
            check.setDescription(description);
            check.setCreatedAt(Timestamp.from(Instant.now()));
            urlCheckRepository.save(check);

            ctx.sessionAttribute("flash", "Проверка успешно выполнена");
            ctx.sessionAttribute("flashType", "success");
            log.info("POST /urls/{id}/checks: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
        } catch (IOException e) {
            log.error("HTTP request failed for URL {}: {}", url.get().getName(), e);
            ctx.sessionAttribute("flash", "Ошибка проверки");
            ctx.sessionAttribute("flashType", "danger");
            log.info("POST /urls/{id}/checks: Set flash: {}, flashType: {}",
                    ctx.sessionAttribute("flash"), ctx.sessionAttribute("flashType"));
        }

        ctx.redirect(NamedRoutes.urlPath(id));
    }
}
