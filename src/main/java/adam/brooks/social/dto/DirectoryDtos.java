package adam.brooks.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DirectoryDtos {

    // minimal, safe-to-expose view of a user — no password, no email, no block list
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummary {
        private String id;
        private String username;
        private String avatarUrl;
    }

    // one row in "your conversations" list — the other person + a preview of the last message
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConversationSummary {
        private String otherUserId;
        private String otherUsername;
        private String lastMessage;
        private String lastMessageAt;
    }
}
