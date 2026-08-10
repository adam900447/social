package adam.brooks.social.service;

import adam.brooks.social.dto.PostDtos.CreatePostRequest;
import adam.brooks.social.model.Post;
import adam.brooks.social.model.User;
import adam.brooks.social.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    public Post createPost(String authorId, CreatePostRequest req) {
        User author = userService.getById(authorId);

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setAuthorName(author.getUsername());
        post.setContent(req.getContent());
        post.setImageUrl(req.getImageUrl());

        return postRepository.save(post);
    }

    /**
     * Feed with private-account filtering applied: a post from someone whose
     * postVisibility is ONLY_ME is hidden from everyone except themselves.
     * Note: filtering happens after the page is fetched, so a page can come
     * back with fewer posts than requested if some were hidden — acceptable
     * for this scale, but worth knowing if you add real pagination controls.
     */
    public Page<Post> getFeed(Pageable pageable, String requestingUserId) {
        Page<Post> page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<Post> visible = filterVisible(page.getContent(), requestingUserId);
        return new PageImpl<>(visible, pageable, page.getTotalElements());
    }

    public Page<Post> getPostsByUser(String userId, Pageable pageable, String requestingUserId) {
        Page<Post> page = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);
        List<Post> visible = filterVisible(page.getContent(), requestingUserId);
        return new PageImpl<>(visible, pageable, page.getTotalElements());
    }

    private List<Post> filterVisible(List<Post> posts, String requestingUserId) {
        Map<String, User.PostVisibility> visibilityCache = new HashMap<>();

        return posts.stream()
                .filter(p -> {
                    if (p.getAuthorId().equals(requestingUserId)) return true; // always see your own posts
                    User.PostVisibility visibility = visibilityCache.computeIfAbsent(
                            p.getAuthorId(), id -> userService.getById(id).getPostVisibility()
                    );
                    return visibility == User.PostVisibility.PUBLIC;
                })
                .toList();
    }

    public void deletePost(String postId, String requestingUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getAuthorId().equals(requestingUserId)) {
            throw new SecurityException("You can only delete your own posts");
        }

        postRepository.deleteById(postId);
    }

    public Post toggleLike(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getLikedByUserIds().contains(userId)) {
            post.getLikedByUserIds().remove(userId);
        } else {
            post.getLikedByUserIds().add(userId);
        }

        return postRepository.save(post);
    }

    public Post sharePost(String originalPostId, String sharingUserId) {
        Post original = postRepository.findById(originalPostId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User sharer = userService.getById(sharingUserId);

        Post shared = new Post();
        shared.setAuthorId(sharingUserId);
        shared.setAuthorName(sharer.getUsername());
        shared.setContent(original.getContent());
        shared.setImageUrl(original.getImageUrl());
        shared.setSharedFromPostId(original.getId());
        shared.setSharedFromAuthorName(original.getAuthorName());

        return postRepository.save(shared);
    }
}
