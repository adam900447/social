package adam.brooks.social.repository;

import adam.brooks.social.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

    // 1-to-1 history, oldest-first pagination handled in service; not filtering
    // deletedFor here because Spring Data derived queries can't easily express
    // "does not contain" cleanly across driver versions — that filter is applied
    // in ChatService after fetching, or via a MongoTemplate query if you want it
    // done in the database (see ChatService for the recommended approach).
    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    Page<Message> findByGroupIdOrderByCreatedAtDesc(String groupId, Pageable pageable);
}
