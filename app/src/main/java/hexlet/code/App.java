package hexlet.code;

import hexlet.code.controller.UrlsController;
import hexlet.code.repository.UrlRepository;
import hexlet.code.repository.UrlCheckRepository;

import io.javalin.Javalin;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import lombok.extern.slf4j.Slf4j;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.rendering.template.JavalinJte;
import gg.jte.resolve.ResourceCodeResolver;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
public class App {
    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    public static Javalin getApp() throws SQLException {
        DataSource dataSource = DataSourceConfig.getDataSource();
        DatabaseInitializer.initialize(dataSource);
        UrlRepository urlRepository = new UrlRepository(dataSource);
        UrlCheckRepository urlCheckRepository = new UrlCheckRepository(dataSource);

        UrlsController.init(urlRepository, urlCheckRepository);

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));

            config.jetty.modifyServer(server -> {
                SessionHandler sessionHandler = new SessionHandler();
                sessionHandler.setSessionIdManager(new DefaultSessionIdManager(server));
                DefaultSessionCache sessionCache = new DefaultSessionCache(sessionHandler);
                sessionCache.setSessionDataStore(new NullSessionDataStore());
                sessionHandler.setSessionCache(sessionCache);
                server.setHandler(sessionHandler);
            });
        });

        app.get(NamedRoutes.rootPath(), UrlsController::index);
        app.post(NamedRoutes.urlsPath(), UrlsController::create);
        app.get(NamedRoutes.urlsPath(), UrlsController::list);
        app.get(NamedRoutes.urlsPath() + "/{id}", UrlsController::show);
        app.post(NamedRoutes.urlsPath() + "/{id}/checks", UrlsController::check);
        app.post("/api/urls", UrlsController::createApi);

        return app;
    }
    public static void main(String[] args) throws SQLException {
        Javalin app = getApp();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        app.start(port);
    }
}
