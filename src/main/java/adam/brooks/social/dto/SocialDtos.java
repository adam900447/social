package adam.brooks.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SocialDtos {

    @Data
    public static class ReportRequest {
        private String reason;
    }

    // a pending request shown in someone's incoming/outgoing list
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FriendRequestView {
        private String requestId;
        private String userId;   // the other person in the request
        private String username;
        private String avatarUrl;
        private String createdAt;
    }

    // profile preview shown before adding someone — includes friendship
    // status relative to the person viewing it, so the frontend knows
    // whether to show "Add friend", "Request sent", "Accept", or "Friends"
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileView {
        private String id;
        private String username;
        private String bio;
        private String avatarUrl;
        private String createdAt;
        private String friendshipStatus; // NONE, FRIENDS, REQUEST_SENT, REQUEST_RECEIVED
        private String pendingRequestId; // set when status is REQUEST_SENT or REQUEST_RECEIVED
    }
}
