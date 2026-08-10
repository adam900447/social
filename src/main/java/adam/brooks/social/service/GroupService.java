package adam.brooks.social.service;

import adam.brooks.social.dto.ChatDtos.CreateGroupRequest;
import adam.brooks.social.dto.GroupDtos.GroupDetail;
import adam.brooks.social.dto.GroupDtos.MemberInfo;
import adam.brooks.social.dto.GroupDtos.UpdateGroupRequest;
import adam.brooks.social.model.Group;
import adam.brooks.social.model.User;
import adam.brooks.social.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;

    public Group createGroup(String ownerId, CreateGroupRequest req) {
        Group group = new Group();
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        group.setOwnerId(ownerId);
        group.getMemberIds().add(ownerId);

        return groupRepository.save(group);
    }

    public List<Group> getGroupsForUser(String userId) {
        return groupRepository.findByMemberIdsContaining(userId);
    }

    public GroupDetail getGroupDetail(String groupId, String requestingUserId) {
        Group group = getGroupOrThrow(groupId);

        if (!group.getMemberIds().contains(requestingUserId)) {
            throw new SecurityException("You're not a member of this group");
        }

        User owner = userService.getById(group.getOwnerId());
        List<MemberInfo> members = group.getMemberIds().stream()
                .map(id -> {
                    try {
                        User u = userService.getById(id);
                        return new MemberInfo(u.getId(), u.getUsername());
                    } catch (Exception e) {
                        return new MemberInfo(id, "(deleted user)");
                    }
                })
                .toList();

        return new GroupDetail(group.getId(), group.getName(), group.getDescription(),
                group.getOwnerId(), owner.getUsername(), members);
    }

    public Group updateGroup(String groupId, String requestingUserId, UpdateGroupRequest req) {
        Group group = getGroupOrThrow(groupId);
        requireOwner(group, requestingUserId);

        if (req.getName() != null && !req.getName().isBlank()) {
            group.setName(req.getName());
        }
        if (req.getDescription() != null) {
            group.setDescription(req.getDescription());
        }

        return groupRepository.save(group);
    }

    /**
     * Any current member can invite someone new — not just the owner.
     * Removing members and deleting the group are still owner-only (below).
     */
    public Group addMember(String groupId, String requestingUserId, String newMemberId) {
        Group group = getGroupOrThrow(groupId);
        requireMember(group, requestingUserId);

        if (!group.getMemberIds().contains(newMemberId)) {
            group.getMemberIds().add(newMemberId);
        }
        return groupRepository.save(group);
    }

    public Group removeMember(String groupId, String requestingUserId, String memberIdToRemove) {
        Group group = getGroupOrThrow(groupId);
        requireOwner(group, requestingUserId);

        if (memberIdToRemove.equals(group.getOwnerId())) {
            throw new IllegalArgumentException("The group owner can't be removed — delete the group instead");
        }

        group.getMemberIds().remove(memberIdToRemove);
        return groupRepository.save(group);
    }

    /** A regular member can leave on their own. The owner cannot leave — they must delete the group. */
    public void leaveGroup(String groupId, String requestingUserId) {
        Group group = getGroupOrThrow(groupId);

        if (group.getOwnerId().equals(requestingUserId)) {
            throw new IllegalArgumentException("As the owner, you can't leave — delete the group instead");
        }
        if (!group.getMemberIds().contains(requestingUserId)) {
            throw new IllegalArgumentException("You're not a member of this group");
        }

        group.getMemberIds().remove(requestingUserId);
        groupRepository.save(group);
    }

    public void deleteGroup(String groupId, String requestingUserId) {
        Group group = getGroupOrThrow(groupId);
        requireOwner(group, requestingUserId);
        groupRepository.deleteById(groupId);
    }

    private Group getGroupOrThrow(String groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    private void requireOwner(Group group, String userId) {
        if (!group.getOwnerId().equals(userId)) {
            throw new SecurityException("Only the group owner can do this");
        }
    }

    private void requireMember(Group group, String userId) {
        if (!group.getMemberIds().contains(userId)) {
            throw new SecurityException("Only current members can invite others");
        }
    }
}
