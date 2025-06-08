package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppTests {
    private Javalin app;
    private UrlRepository urlRepository;

    @BeforeAll
    void setUpApp() throws SQLException {
        app = App.getApp();
        urlRepository = new UrlRepository(DataSourceConfig.getDataSource());
    }

    @AfterAll
    void tearDownApp() {
        app.stop();
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (var conn = DataSourceConfig.getDataSource().getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE page_analyzer RESTART IDENTITY");
        }
        // Очистка состояния Javalin перед каждым тестом
        app = App.getApp();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void testGetMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("URL Analyzer");
        });
    }

    @Test
    void testPostUrlValid() {
        JavalinTest.test(app, (server, client) -> {
            // Создаём OkHttpClient без следования за редиректами
            OkHttpClient noRedirectClient = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();

            // Отправляем POST /urls
            String url = "http://localhost:" + server.port() + "/urls";
            var requestBody = "url=" + URLEncoder.encode("https://example.com", StandardCharsets.UTF_8);
            var request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/x-www-form-urlencoded")))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Content-Length", String.valueOf(requestBody.length()))
                    .build();
            var response = noRedirectClient.newCall(request).execute();

            // Проверяем статус 302
            assertThat(response.code()).isEqualTo(302);

            // Проверяем, что URL сохранён в базе
            var urls = urlRepository.findAll();
            assertThat(urls).hasSize(1);
            assertThat(urls.get(0).getName()).isEqualTo("https://example.com");
        });
    }


    @Test
    void testPostUrlInvalid() {
        JavalinTest.test(app, (server, client) -> {
            // Создаём OkHttpClient без следования за редиректами
            OkHttpClient noRedirectClient = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();

            // Отправляем POST /urls с невалидным URL (пустым)
            String url = "http://localhost:" + server.port() + "/urls";
            var requestBody = "url=" + URLEncoder.encode("", StandardCharsets.UTF_8);
            var request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/x-www-form-urlencoded")))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Content-Length", String.valueOf(requestBody.length()))
                    .build();
            var response = noRedirectClient.newCall(request).execute();

            // Проверяем статус 400 (или 302, если редирект на /urls)
            assertThat(response.code()).isEqualTo(302);

            // Проверяем, что URL не сохранён в базе
            var urls = urlRepository.findAll();
            assertThat(urls).isEmpty();
        });
    }

    @Test
    void testPostUrlDuplicate() {
        JavalinTest.test(app, (server, client) -> {
            // Создаём OkHttpClient без следования за редиректами
            OkHttpClient noRedirectClient = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();

            // Отправляем первый POST /urls для создания URL
            String url = "http://localhost:" + server.port() + "/urls";
            var requestBody = "url=" + URLEncoder.encode("https://example.com", StandardCharsets.UTF_8);
            var firstRequest = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/x-www-form-urlencoded")))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Content-Length", String.valueOf(requestBody.length()))
                    .build();
            var firstResponse = noRedirectClient.newCall(firstRequest).execute();
            assertThat(firstResponse.code()).isEqualTo(302);

            // Проверяем, что первый URL сохранён
            var urlsAfterFirst = urlRepository.findAll();
            assertThat(urlsAfterFirst).hasSize(1);
            assertThat(urlsAfterFirst.get(0).getName()).isEqualTo("https://example.com");

            // Отправляем второй POST /urls с тем же URL
            var secondRequest = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/x-www-form-urlencoded")))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Content-Length", String.valueOf(requestBody.length()))
                    .build();
            var secondResponse = noRedirectClient.newCall(secondRequest).execute();

            // Проверяем статус 302 (редирект на /urls)
            assertThat(secondResponse.code()).isEqualTo(302);

            // Проверяем, что в базе остался только один URL
            var urlsAfterSecond = urlRepository.findAll();
            assertThat(urlsAfterSecond).hasSize(1);
            assertThat(urlsAfterSecond.get(0).getName()).isEqualTo("https://example.com");
        });
    }

    @Test
    void testGetUrlsPage() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            var url = new Url("https://test.com", Timestamp.from(Instant.now()));
            urlRepository.save(url);

            var response = client.get("/urls");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("https://test.com");
        });
    }

    @Test
    void testGetUrlById() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            var url = new Url("https://detail.com", Timestamp.from(Instant.now()));
            urlRepository.save(url);

            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("https://detail.com");
        });
    }

    @Test
    void testGetUrlNotFound() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/999");
            assertThat(response.code()).isEqualTo(404);
            assertThat(response.body().string()).contains("URL не найден");
        });
    }
}
