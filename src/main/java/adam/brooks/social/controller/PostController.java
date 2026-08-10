package adam.brooks.social.controller;

import adam.brooks.social.dto.PostDtos.CreatePostRequest;
import adam.brooks.social.model.Post;
import adam.brooks.social.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest req, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(postService.createPost(userId, req));
    }

    @GetMapping
    public ResponseEntity<Page<Post>> getFeed(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(postService.getFeed(PageRequest.of(page, size), userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Post>> getUserPosts(@PathVariable String userId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    Authentication authentication) {
        String requesterId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(postService.getPostsByUser(userId, PageRequest.of(page, size), requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Post> toggleLike(@PathVariable String id, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(postService.toggleLike(id, userId));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<Post> sharePost(@PathVariable String id, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(postService.sharePost(id, userId));
    }
}
