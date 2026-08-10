package adam.brooks.social.service;

import adam.brooks.social.dto.DirectoryDtos.ConversationSummary;
import adam.brooks.social.model.Message;
import adam.brooks.social.model.User;
import adam.brooks.social.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final UserService userService;

    // ---------- Direct (1-to-1) messages ----------

    public Message sendDirectMessage(String senderId, String receiverId, String content) {
        if (userService.isBlockedEitherWay(senderId, receiverId)) {
            throw new SecurityException("You cannot message this user");
        }

        User receiver = userService.getById(receiverId);
        if (receiver.getMessagePrivacy() == User.MessagePrivacy.NO_ONE) {
            throw new SecurityException("This user isn't accepting messages right now");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setConversationId(Message.buildConversationId(senderId, receiverId));
        message.setContent(content);
        message.setType(Message.MessageType.TEXT);

        return messageRepository.save(message);
    }

    public List<Message> getDirectHistory(String userIdA, String userIdB, String requestingUserId) {
        String conversationId = Message.buildConversationId(userIdA, userIdB);

        Query query = new Query(
                Criteria.where("conversationId").is(conversationId)
                        .and("deletedFor").nin(requestingUserId)
        ).with(Sort.by(Sort.Direction.ASC, "createdAt"));

        return mongoTemplate.find(query, Message.class);
    }

    public List<ConversationSummary> getConversationsForUser(String userId) {
        Query query = new Query(
                new Criteria().orOperator(
                        Criteria.where("senderId").is(userId),
                        Criteria.where("receiverId").is(userId)
                )
        ).addCriteria(Criteria.where("deletedFor").nin(userId))
         .with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Message> messages = mongoTemplate.find(query, Message.class);

        Map<String, Message> latestByOtherUser = new LinkedHashMap<>();
        for (Message m : messages) {
            if (m.getReceiverId() == null) continue;
            String otherUserId = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            latestByOtherUser.putIfAbsent(otherUserId, m);
        }

        List<ConversationSummary> result = new ArrayList<>();
        for (Map.Entry<String, Message> entry : latestByOtherUser.entrySet()) {
            String otherUserId = entry.getKey();
            Message last = entry.getValue();
            try {
                String otherUsername = userService.getById(otherUserId).getUsername();
                result.add(new ConversationSummary(
                        otherUserId, otherUsername, last.getContent(), last.getCreatedAt().toString()
                ));
            } catch (Exception e) {
                // the other user no longer exists — skip this conversation
            }
        }
        return result;
    }

    public void deleteMessageForMe(String messageId, String requestingUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        boolean isParticipant = requestingUserId.equals(message.getSenderId())
                || requestingUserId.equals(message.getReceiverId());
        if (!isParticipant) {
            throw new SecurityException("You are not part of this conversation");
        }

        if (!message.getDeletedFor().contains(requestingUserId)) {
            message.getDeletedFor().add(requestingUserId);
            messageRepository.save(message);
        }
    }

    public void deleteConversationForMe(String otherUserId, String requestingUserId) {
        String conversationId = Message.buildConversationId(requestingUserId, otherUserId);

        Query query = new Query(Criteria.where("conversationId").is(conversationId));
        mongoTemplate.updateMulti(
                query,
                new org.springframework.data.mongodb.core.query.Update()
                        .addToSet("deletedFor", requestingUserId),
                Message.class
        );
    }

    // ---------- Group messages ----------

    public Message sendGroupMessage(String senderId, String groupId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setGroupId(groupId);
        message.setContent(content);
        message.setType(Message.MessageType.TEXT);

        return messageRepository.save(message);
    }

    public List<Message> getGroupHistory(String groupId, String requestingUserId) {
        Query query = new Query(
                Criteria.where("groupId").is(groupId)
                        .and("deletedFor").nin(requestingUserId)
        ).with(Sort.by(Sort.Direction.ASC, "createdAt"));

        return mongoTemplate.find(query, Message.class);
    }
}
