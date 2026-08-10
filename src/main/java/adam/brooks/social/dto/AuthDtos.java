package adam.brooks.social.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String username;
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String usernameOrEmail;
        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String userId;
        private String username;

        public AuthResponse(String token, String userId, String username) {
            this.token = token;
            this.userId = userId;
            this.username = username;
        }
    }
}
