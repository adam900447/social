package adam.brooks.social.controller;

import adam.brooks.social.dto.DirectoryDtos.UserSummary;
import adam.brooks.social.dto.SocialDtos.FriendRequestView;
import adam.brooks.social.model.FriendRequest;
import adam.brooks.social.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request/{targetUserId}")
    public ResponseEntity<FriendRequest> sendRequest(@PathVariable String targetUserId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(friendService.sendRequest(userId, targetUserId));
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Void> accept(@PathVariable String requestId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        friendService.acceptRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decline/{requestId}")
    public ResponseEntity<Void> decline(@PathVariable String requestId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        friendService.declineRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<Void> cancel(@PathVariable String requestId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        friendService.cancelRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<Void> unfriend(@PathVariable String friendUserId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        friendService.unfriend(userId, friendUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<UserSummary>> getFriends(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestView>> getIncoming(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(friendService.getIncomingRequests(userId));
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestView>> getOutgoing(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(friendService.getOutgoingRequests(userId));
    }
}
