package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.rendering.template.JavalinJte;
import gg.jte.resolve.ResourceCodeResolver;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class App {
    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    public static Javalin getApp() {
        DataSource dataSource = DataSourceConfig.getDataSource();
        DatabaseInitializer.initialize(dataSource);
        UrlRepository urlRepository = new UrlRepository(dataSource);

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.get("/", ctx -> {
            ctx.attribute("flash", ctx.consumeSessionAttribute("flash"));
            ctx.attribute("flashType", ctx.consumeSessionAttribute("flashType"));
            ctx.render("index.jte");
        });

        app.post("/urls", ctx -> {
            String inputUrl = ctx.formParam("url");
            log.info("Received URL: {}", inputUrl);
            if (inputUrl == null || inputUrl.isBlank()) {
                log.warn("Invalid URL: null or blank");
                ctx.sessionAttribute("flash", "Некорректный URL");
                ctx.sessionAttribute("flashType", "danger");
                ctx.redirect("/");
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
                normalizedUrl = sb.toString();
                log.info("Normalized URL: {}", normalizedUrl);
            } catch (MalformedURLException | IllegalArgumentException e) {
                log.error("URL parsing error: {}", inputUrl, e);
                ctx.sessionAttribute("flash", "Некорректный URL");
                ctx.sessionAttribute("flashType", "danger");
                ctx.redirect("/");
                return;
            }

            Optional<Url> existingUrl = urlRepository.findByName(normalizedUrl);
            if (existingUrl.isPresent()) {
                log.info("URL already exists: {}", normalizedUrl);
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flashType", "info");
                ctx.redirect("/");
                return;
            }
            try {
                Url url = new Url(normalizedUrl, Timestamp.from(Instant.now()));
                urlRepository.save(url);
                log.info("URL saved: {}", normalizedUrl);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", "success");
                ctx.redirect("/urls");
            } catch (Exception e) {
                log.error("Error saving URL: {}", normalizedUrl, e);
                ctx.sessionAttribute("flash", "Ошибка при добавлении URL");
                ctx.sessionAttribute("flashType", "danger");
                ctx.redirect("/");
            }
        });

        app.get("/urls", ctx -> {
            List<Url> urls = urlRepository.findAll();
            log.info("URLs retrieved: {}, content: {}", urls.size(), urls);
            String flash = ctx.consumeSessionAttribute("flash");
            String flashType = ctx.consumeSessionAttribute("flashType");
            ctx.attribute("urls", urls != null ? urls : Collections.emptyList());
            ctx.attribute("flash", flash);
            ctx.attribute("flashType", flashType);
            log.info("Flash message: {}, type: {}", flash, flashType);
            ctx.render("urls.jte", Map.of(
                    "urls", urls != null ? urls : Collections.emptyList(),
                    "flash", flash != null ? flash : "",
                    "flashType", flashType != null ? flashType : ""
            ));
        });

        app.get("/urls/{id}", ctx -> {
            Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
            if (id == null) {
                ctx.status(400);
                ctx.attribute("flash", "Неверный ID");
                ctx.attribute("flashType", "danger");
                log.warn("Invalid ID provided");
                ctx.render("urls/show.jte", Map.of("url", null, "flash", "Неверный ID", "flashType", "danger"));
                return;
            }
            Optional<Url> url = urlRepository.findById(id);
            if (url.isEmpty()) {
                ctx.status(404);
                ctx.attribute("flash", "URL не найден");
                ctx.attribute("flashType", "danger");
                log.warn("URL not found for ID: {}", id);
                ctx.render("urls/show.jte", Map.of("url", null, "flash", "URL не найден", "flashType", "danger"));
                return;
            }
            String flash = ctx.consumeSessionAttribute("flash");
            String flashType = ctx.consumeSessionAttribute("flashType");
            Url urlObj = url.get();
            log.info("Rendering URL details for ID: {}, URL: {}", id, urlObj);
            ctx.render("urls/show.jte", Map.of(
                    "url", urlObj,
                    "flash", flash != null ? flash : "",
                    "flashType", flashType != null ? flashType : ""
            ));
        });

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        app.start(port);
    }
}
