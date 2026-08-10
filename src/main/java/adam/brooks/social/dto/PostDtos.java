package adam.brooks.social.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class PostDtos {

    @Data
    public static class CreatePostRequest {
        @NotBlank
        private String content;
        private String imageUrl; // optional
    }

    @Data
    public static class CommentRequest {
        @NotBlank
        private String content;
    }
}
