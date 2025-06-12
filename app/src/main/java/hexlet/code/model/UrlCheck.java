package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class UrlCheck {
    private Long id;
    private Long urlId;
    private int statusCode;
    private String title;
    private String h1;
    private String description;
    private Timestamp createdAt;

    public UrlCheck(Long urlId, int statusCode, String title, String h1, String description, Timestamp createdAt) {
        this.urlId = urlId;
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.createdAt = createdAt;
    }
}

