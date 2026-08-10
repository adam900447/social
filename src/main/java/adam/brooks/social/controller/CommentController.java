package adam.brooks.social.controller;

import adam.brooks.social.dto.PostDtos.CommentRequest;
import adam.brooks.social.model.Comment;
import adam.brooks.social.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable String postId,
                                               @Valid @RequestBody CommentRequest req,
                                               Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(commentService.addComment(postId, userId, req));
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable String postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String postId,
                                               @PathVariable String commentId,
                                               Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
