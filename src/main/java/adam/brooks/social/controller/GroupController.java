package adam.brooks.social.controller;

import adam.brooks.social.dto.ChatDtos.CreateGroupRequest;
import adam.brooks.social.dto.GroupDtos.GroupDetail;
import adam.brooks.social.dto.GroupDtos.UpdateGroupRequest;
import adam.brooks.social.model.Group;
import adam.brooks.social.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupRequest req, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(groupService.createGroup(userId, req));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Group>> getMyGroups(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(groupService.getGroupsForUser(userId));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetail> getGroupDetail(@PathVariable String groupId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(groupService.getGroupDetail(groupId, userId));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<Group> updateGroup(@PathVariable String groupId,
                                              @RequestBody UpdateGroupRequest req,
                                              Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(groupService.updateGroup(groupId, userId, req));
    }

    @PostMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> addMember(@PathVariable String groupId,
                                            @PathVariable String memberId,
                                            Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(groupService.addMember(groupId, userId, memberId));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Group> removeMember(@PathVariable String groupId,
                                               @PathVariable String memberId,
                                               Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(groupService.removeMember(groupId, userId, memberId));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable String groupId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        groupService.deleteGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }
}
