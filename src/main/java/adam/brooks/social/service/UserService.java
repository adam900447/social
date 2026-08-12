package adam.brooks.social.service;

import adam.brooks.social.dto.AuthDtos.LoginRequest;
import adam.brooks.social.dto.AuthDtos.RegisterRequest;
import adam.brooks.social.dto.ProfileDtos.UpdateProfileRequest;
import adam.brooks.social.model.User;
import adam.brooks.social.repository.UserRepository;
import adam.brooks.social.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        
        // Auto-verify users for now so login isn't blocked by SMTP port limits on Render
        user.setVerified(true); 
        user.setVerificationToken(UUID.randomUUID().toString());

        User saved = userRepository.save(user);

        // Async call (with @Async on EmailService, this returns in <1 millisecond)
        emailService.sendVerificationEmail(saved.getEmail(), saved.getUsername(), saved.getVerificationToken());

        return saved;
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification link"));

        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public record LoginResult(String token, User user) {}

    public LoginResult login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(req.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in. Check your inbox for the confirmation link.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResult(token, user);
    }

    public User getById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public java.util.List<User> searchByUsername(String fragment, String excludeUserId) {
        return userRepository.findByUsernameContainingIgnoreCase(fragment).stream()
                .filter(u -> !u.getId().equals(excludeUserId))
                .toList();
    }

    public User updateProfile(String userId, UpdateProfileRequest req) {
        User user = getById(userId);

        if (req.getBio() != null) {
            user.setBio(req.getBio());
        }
        if (req.getMessagePrivacy() != null) {
            user.setMessagePrivacy(req.getMessagePrivacy());
        }
        if (req.getPostVisibility() != null) {
            user.setPostVisibility(req.getPostVisibility());
        }

        return userRepository.save(user);
    }

    public User updateAvatar(String userId, String avatarUrl) {
        User user = getById(userId);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    public void blockUser(String currentUserId, String targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot block yourself");
        }
        User user = getById(currentUserId);
        if (!user.getBlockedUserIds().contains(targetUserId)) {
            user.getBlockedUserIds().add(targetUserId);
            userRepository.save(user);
        }
    }

    public void unblockUser(String currentUserId, String targetUserId) {
        User user = getById(currentUserId);
        user.getBlockedUserIds().remove(targetUserId);
        userRepository.save(user);
    }

    public boolean isBlockedEitherWay(String userIdA, String userIdB) {
        User a = getById(userIdA);
        if (a.getBlockedUserIds().contains(userIdB)) return true;
        User b = getById(userIdB);
        return b.getBlockedUserIds().contains(userIdA);
    }
}
