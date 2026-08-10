package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String password; // stored as a BCrypt hash, never plain text

    private String bio;
    private String avatarUrl;

    private List<String> blockedUserIds = new ArrayList<>();
    private List<String> friendIds = new ArrayList<>();

    private boolean verified = false;
    private String verificationToken;

    private MessagePrivacy messagePrivacy = MessagePrivacy.EVERYONE;
    private PostVisibility postVisibility = PostVisibility.PUBLIC;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MessagePrivacy { EVERYONE, NO_ONE }
    public enum PostVisibility { PUBLIC, ONLY_ME }
}
