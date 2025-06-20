package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class Url {
    private Long id;
    private String name;
    private Timestamp createdAt;

    public Url(String name, Timestamp createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    /**
     * Returns a string representation of the Url object.
     * This method is not intended for overriding; for custom string representation,
     * extend this class and override with caution to maintain consistency.
     * @return a string representation of the Url
     */
    @Override
    public String toString() {
        return "Url{id=" + id + ", name='" + name + "', createdAt=" + createdAt + "}";
    }
}
