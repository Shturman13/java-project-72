package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
public class App {
    public static Javalin getApp() {
        DataSource dataSource = DataSourceConfig.getDataSource();

        DatabaseInitializer.initialize(dataSource);
        UrlRepository urlRepository = new UrlRepository(dataSource);


        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
        });

        app.get("/", ctx -> ctx.result("Hello World"));

        app.post("/urls", ctx -> {
            String name = ctx.formParam("name");
            Url url = new Url(name, Timestamp.from(Instant.now()));
            urlRepository.save(url);
            ctx.status(201).result("URL saved: " + url.getName());
        });

        app.get("/urls", ctx -> {
            ctx.json(urlRepository.findAll());
        });
        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        app.start(port);
    }
}
