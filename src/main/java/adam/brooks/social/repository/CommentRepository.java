package adam.brooks.social.repository;

import adam.brooks.social.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(String postId);
    long countByPostId(String postId);
}
