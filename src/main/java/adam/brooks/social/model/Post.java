package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    private String authorId;
    private String authorName; // denormalized so the feed doesn't need a join per post

    private String content;
    private String imageUrl;

    private List<String> likedByUserIds = new ArrayList<>();

    // set only when this post is a share/repost of another post
    private String sharedFromPostId;
    private String sharedFromAuthorName;

    private LocalDateTime createdAt = LocalDateTime.now();
}
