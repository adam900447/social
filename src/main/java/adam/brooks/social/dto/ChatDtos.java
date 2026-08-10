package adam.brooks.social.dto;

import adam.brooks.social.model.Message;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ChatDtos {

    // Sent over WebSocket (/app/chat.send) — direct message
    @Data
    public static class DirectMessageRequest {
        @NotBlank
        private String receiverId;
        @NotBlank
        private String content;
    }

    // Sent over WebSocket (/app/chat.group.send) — group message
    @Data
    public static class GroupMessageRequest {
        @NotBlank
        private String groupId;
        @NotBlank
        private String content;
    }

    // What gets broadcast to clients
    @Data
    public static class MessagePayload {
        private String id;
        private String senderId;
        private String senderName;
        private String receiverId;
        private String groupId;
        private String content;
        private Message.MessageType type;
        private String createdAt;
    }

    @Data
    public static class CreateGroupRequest {
        @NotBlank
        private String name;
        private String description;
    }
}
