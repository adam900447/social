package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    private String postId;
    private String authorId;
    private String authorName;

    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();
}
