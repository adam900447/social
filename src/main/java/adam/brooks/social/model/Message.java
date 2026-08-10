package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    private String senderId;

    // exactly one of these two is set: a message is either 1-to-1 or in a group
    private String receiverId; // for direct messages
    private String groupId;    // for group messages

    // for direct messages: a stable id combining both user ids (sorted),
    // e.g. "60a1_60b2" — makes fetching a conversation's history a single indexed query
    @Indexed
    private String conversationId;

    private String content;

    private MessageType type = MessageType.TEXT;

    // user ids who have deleted this message "for me" — hides it from their view only
    private List<String> deletedFor = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MessageType {
        TEXT, CALL_LOG
    }

    public static String buildConversationId(String userIdA, String userIdB) {
        return userIdA.compareTo(userIdB) < 0
                ? userIdA + "_" + userIdB
                : userIdB + "_" + userIdA;
    }
}
