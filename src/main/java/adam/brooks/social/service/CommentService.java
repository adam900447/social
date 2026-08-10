package adam.brooks.social.service;

import adam.brooks.social.dto.PostDtos.CommentRequest;
import adam.brooks.social.model.Comment;
import adam.brooks.social.model.User;
import adam.brooks.social.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;

    public Comment addComment(String postId, String authorId, CommentRequest req) {
        User author = userService.getById(authorId);

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setAuthorName(author.getUsername());
        comment.setContent(req.getContent());

        return commentRepository.save(comment);
    }

    public List<Comment> getComments(String postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public void deleteComment(String commentId, String requestingUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAuthorId().equals(requestingUserId)) {
            throw new SecurityException("You can only delete your own comments");
        }

        commentRepository.deleteById(commentId);
    }
}
