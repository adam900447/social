package adam.brooks.social.controller;

import adam.brooks.social.dto.DirectoryDtos.UserSummary;
import adam.brooks.social.dto.ProfileDtos.UpdateProfileRequest;
import adam.brooks.social.dto.SocialDtos.ProfileView;
import adam.brooks.social.dto.SocialDtos.ReportRequest;
import adam.brooks.social.model.FriendRequest;
import adam.brooks.social.model.SecurityActivityLog;
import adam.brooks.social.model.User;
import adam.brooks.social.repository.SecurityActivityLogRepository;
import adam.brooks.social.repository.UserRepository;
import adam.brooks.social.service.FileStorageService;
import adam.brooks.social.service.FriendService;
import adam.brooks.social.service.ReportService;
import adam.brooks.social.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final FriendService friendService;
    private final ReportService reportService;
    private final SecurityActivityLogRepository securityActivityLogRepository;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getById(currentUserId));
    }

    // "Where you're logged in" — the account owner's own login/security
    // history: IP, browser, OS, device, when, and whether it succeeded.
    // Only ever returns the caller's own entries, never anyone else's.
    @GetMapping("/me/security-log")
    public ResponseEntity<List<SecurityActivityLog>> getMySecurityLog(Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(securityActivityLogRepository.findByUserIdOrderByTimestampDesc(currentUserId));
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest req, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(currentUserId, req));
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<User> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        String url = fileStorageService.saveAvatar(file);
        return ResponseEntity.ok(userService.updateAvatar(currentUserId, url));
    }

    @GetMapping
    public ResponseEntity<List<UserSummary>> listUsers(Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        List<UserSummary> users = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getAvatarUrl()))
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSummary>> searchUsers(@RequestParam String query, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        List<UserSummary> results = userService.searchByUsername(query, currentUserId).stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getAvatarUrl()))
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<ProfileView> getProfile(@PathVariable String id, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        User target = userService.getById(id);
        User me = userService.getById(currentUserId);

        String status;
        String pendingRequestId = null;

        if (id.equals(currentUserId)) {
            status = "SELF";
        } else if (me.getFriendIds().contains(id)) {
            status = "FRIENDS";
        } else {
            Optional<FriendRequest> pending = friendService.findPendingBetween(currentUserId, id);
            if (pending.isPresent()) {
                FriendRequest req = pending.get();
                pendingRequestId = req.getId();
                status = req.getFromUserId().equals(currentUserId) ? "REQUEST_SENT" : "REQUEST_RECEIVED";
            } else {
                status = "NONE";
            }
        }

        return ResponseEntity.ok(new ProfileView(
                target.getId(), target.getUsername(), target.getBio(), target.getAvatarUrl(),
                target.getCreatedAt().toString(), status, pendingRequestId
        ));
    }

    @PostMapping("/block/{targetUserId}")
    public ResponseEntity<Void> blockUser(@PathVariable String targetUserId, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        userService.blockUser(currentUserId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock/{targetUserId}")
    public ResponseEntity<Void> unblockUser(@PathVariable String targetUserId, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        userService.unblockUser(currentUserId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/report/{targetUserId}")
    public ResponseEntity<Void> reportUser(@PathVariable String targetUserId,
                                            @RequestBody ReportRequest req,
                                            Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        reportService.reportUser(currentUserId, targetUserId, req.getReason());
        return ResponseEntity.ok().build();
    }
}
