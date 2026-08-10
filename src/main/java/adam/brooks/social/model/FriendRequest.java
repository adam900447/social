package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "friend_requests")
public class FriendRequest {

    @Id
    private String id;

    private String fromUserId;
    private String toUserId;

    private Status status = Status.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { PENDING, ACCEPTED }
}
