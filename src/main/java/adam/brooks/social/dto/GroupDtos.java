package adam.brooks.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GroupDtos {

    @Data
    public static class UpdateGroupRequest {
        private String name;
        private String description;
    }

    // enriched view for the edit screen — includes resolved member usernames,
    // not just raw ids, so the frontend doesn't need a separate lookup per member
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupDetail {
        private String id;
        private String name;
        private String description;
        private String ownerId;
        private String ownerUsername;
        private List<MemberInfo> members;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberInfo {
        private String id;
        private String username;
    }
}
