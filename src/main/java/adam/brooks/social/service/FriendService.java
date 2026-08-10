package adam.brooks.social.service;

import adam.brooks.social.dto.DirectoryDtos.UserSummary;
import adam.brooks.social.dto.SocialDtos.FriendRequestView;
import adam.brooks.social.model.FriendRequest;
import adam.brooks.social.model.User;
import adam.brooks.social.repository.FriendRequestRepository;
import adam.brooks.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public FriendRequest sendRequest(String fromUserId, String toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("You can't send yourself a friend request");
        }
        if (userService.isBlockedEitherWay(fromUserId, toUserId)) {
            throw new SecurityException("You can't send a friend request to this user");
        }

        User from = userService.getById(fromUserId);
        if (from.getFriendIds().contains(toUserId)) {
            throw new IllegalArgumentException("You're already friends");
        }

        // if they already sent one to us, don't create a duplicate — this is
        // a signal the frontend should offer "Accept" instead of "Add friend"
        if (friendRequestRepository.findByFromUserIdAndToUserIdAndStatus(toUserId, fromUserId, FriendRequest.Status.PENDING).isPresent()) {
            throw new IllegalArgumentException("This user already sent you a request — accept it instead");
        }
        if (friendRequestRepository.findByFromUserIdAndToUserIdAndStatus(fromUserId, toUserId, FriendRequest.Status.PENDING).isPresent()) {
            throw new IllegalArgumentException("You already sent a request to this user");
        }

        FriendRequest req = new FriendRequest();
        req.setFromUserId(fromUserId);
        req.setToUserId(toUserId);
        return friendRequestRepository.save(req);
    }

    public void acceptRequest(String requestId, String requestingUserId) {
        FriendRequest req = getOrThrow(requestId);
        if (!req.getToUserId().equals(requestingUserId)) {
            throw new SecurityException("This request isn't addressed to you");
        }

        req.setStatus(FriendRequest.Status.ACCEPTED);
        friendRequestRepository.save(req);

        User a = userService.getById(req.getFromUserId());
        User b = userService.getById(req.getToUserId());
        if (!a.getFriendIds().contains(b.getId())) a.getFriendIds().add(b.getId());
        if (!b.getFriendIds().contains(a.getId())) b.getFriendIds().add(a.getId());
        userRepository.save(a);
        userRepository.save(b);
    }

    public void declineRequest(String requestId, String requestingUserId) {
        FriendRequest req = getOrThrow(requestId);
        if (!req.getToUserId().equals(requestingUserId)) {
            throw new SecurityException("This request isn't addressed to you");
        }
        friendRequestRepository.delete(req);
    }

    public void cancelRequest(String requestId, String requestingUserId) {
        FriendRequest req = getOrThrow(requestId);
        if (!req.getFromUserId().equals(requestingUserId)) {
            throw new SecurityException("You can only cancel requests you sent");
        }
        friendRequestRepository.delete(req);
    }

    public void unfriend(String userId, String friendUserId) {
        User a = userService.getById(userId);
        User b = userService.getById(friendUserId);
        a.getFriendIds().remove(friendUserId);
        b.getFriendIds().remove(userId);
        userRepository.save(a);
        userRepository.save(b);
    }

    public List<UserSummary> getFriends(String userId) {
        User user = userService.getById(userId);
        return user.getFriendIds().stream()
                .map(id -> {
                    try {
                        User f = userService.getById(id);
                        return new UserSummary(f.getId(), f.getUsername(), f.getAvatarUrl());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public List<FriendRequestView> getIncomingRequests(String userId) {
        return friendRequestRepository.findByToUserIdAndStatus(userId, FriendRequest.Status.PENDING).stream()
                .map(r -> {
                    User from = userService.getById(r.getFromUserId());
                    return new FriendRequestView(r.getId(), from.getId(), from.getUsername(), from.getAvatarUrl(), r.getCreatedAt().toString());
                })
                .toList();
    }

    public List<FriendRequestView> getOutgoingRequests(String userId) {
        return friendRequestRepository.findByFromUserIdAndStatus(userId, FriendRequest.Status.PENDING).stream()
                .map(r -> {
                    User to = userService.getById(r.getToUserId());
                    return new FriendRequestView(r.getId(), to.getId(), to.getUsername(), to.getAvatarUrl(), r.getCreatedAt().toString());
                })
                .toList();
    }

    /** Used by profile preview to figure out what button state to show. */
    public java.util.Optional<FriendRequest> findPendingBetween(String userIdA, String userIdB) {
        return friendRequestRepository.findByFromUserIdAndToUserIdAndStatus(userIdA, userIdB, FriendRequest.Status.PENDING)
                .or(() -> friendRequestRepository.findByFromUserIdAndToUserIdAndStatus(userIdB, userIdA, FriendRequest.Status.PENDING));
    }

    private FriendRequest getOrThrow(String requestId) {
        return friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
    }
}
