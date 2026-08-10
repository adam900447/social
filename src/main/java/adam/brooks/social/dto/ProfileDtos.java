package adam.brooks.social.dto;

import adam.brooks.social.model.User;
import lombok.Data;

public class ProfileDtos {

    @Data
    public static class UpdateProfileRequest {
        private String bio; // optional — null/omitted means "leave unchanged"
        private User.MessagePrivacy messagePrivacy;
        private User.PostVisibility postVisibility;
    }
}
