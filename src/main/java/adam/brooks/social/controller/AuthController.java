package adam.brooks.social.controller;

import adam.brooks.social.dto.AuthDtos.AuthResponse;
import adam.brooks.social.dto.AuthDtos.LoginRequest;
import adam.brooks.social.dto.AuthDtos.RegisterRequest;
import adam.brooks.social.model.User;
import adam.brooks.social.security.TrackActivity;
import adam.brooks.social.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @TrackActivity(action = "REGISTER")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req);
        return ResponseEntity.ok(new AuthResponse(null, user.getId(), user.getUsername()));
    }

    @TrackActivity(action = "LOGIN")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        UserService.LoginResult result = userService.login(req);
        return ResponseEntity.ok(new AuthResponse(
                result.token(), result.user().getId(), result.user().getUsername()
        ));
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.status(302).header("Location", "/login?verified=true").build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(302).header("Location", "/login?verified=false").build();
        }
    }
}
