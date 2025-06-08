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
import java.util.HashMap;
import java.util.Optional;

@Slf4j
public class App {
    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
//        templateEngine.setCacheEnabled(false);
        return templateEngine;
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
            String flash = ctx.consumeSessionAttribute("flash");
            String flashType = ctx.consumeSessionAttribute("flashType");
            ctx.render("index.jte", Collections.singletonMap("flash", flash != null ? flash : ""));
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

//                String contentType = ctx.header("Content-Type");
//                log.info("Received Headers: {}", ctx.headerMap());
//                log.info("Received Body: {}", ctx.body());
//                log.info("Content-Type: {}", contentType);
//                log.info("Form Params: {}", ctx.formParamMap());
//                if (contentType == null || !contentType.contains("application/x-www-form-urlencoded")) {
//                    log.warn("Invalid Content-Type: {}", contentType);
//                    ctx.status(400);
//                    ctx.sessionAttribute("flash", "Некорректный Content-Type");
//                    ctx.sessionAttribute("flashType", "danger");
//                    ctx.redirect("/");
//                    return;
//                }
//                String inputUrl = ctx.formParam("url");
//                log.info("Received URL: {}", inputUrl);
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
                ctx.redirect("/urls");
                return;
            }
            try {
                Url url = new Url(normalizedUrl, Timestamp.from(Instant.now()));
                urlRepository.save(url);
                log.info("URL saved: {}", normalizedUrl);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", "success");
                ctx.redirect("/urls");
                return;
            } catch (Exception e) {
                log.error("Error saving URL: {}", normalizedUrl, e);
                ctx.sessionAttribute("flash", "Ошибка при добавлении URL");
                ctx.sessionAttribute("flashType", "danger");
                ctx.redirect("/");
                return;
            }
        });

        app.get("/urls", ctx -> {
            List<Url> urls = urlRepository.findAll();
            log.info("URLs retrieved: {}, content: {}", urls.size(), urls);
            String flash = ctx.consumeSessionAttribute("flash");
            String flashType = ctx.consumeSessionAttribute("flashType");
            ctx.render("urls.jte", Map.of(
                    "urls", urls != null ? urls : Collections.emptyList(),
                    "flash", flash != null ? flash : "",
                    "flashType", flashType != null ? flashType : ""
            ));
        });

        app.get("/urls/{id}", ctx -> {
            Long id;
            try {
                id = ctx.pathParamAsClass("id", Long.class).get();
            } catch (Exception e) {
                log.error("Invalid ID format: {}", ctx.pathParam("id"), e);
                ctx.status(400);
                ctx.sessionAttribute("flash", "Некорректный ID");
                ctx.sessionAttribute("flashType", "danger");
                ctx.redirect("/urls");
                return;
            }
            Optional<Url> url = urlRepository.findById(id);
            String flash = ctx.consumeSessionAttribute("flash");
            String flashType = ctx.consumeSessionAttribute("flashType");
            if (url.isEmpty()) {
                ctx.status(404);
                Map<String, Object> model = new HashMap<>();
                model.put("url", null);
                model.put("flash", flash != null ? flash : "URL не найден");
                model.put("flashType", flashType != null ? flashType : "danger");
                ctx.render("urls/show.jte", model);
                return;
            }
            ctx.render("urls/show.jte", Map.of(
                    "url", url.get(),
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


