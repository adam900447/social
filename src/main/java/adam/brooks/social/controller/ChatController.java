package adam.brooks.social.controller;

import adam.brooks.social.dto.DirectoryDtos.ConversationSummary;
import adam.brooks.social.model.CallLog;
import adam.brooks.social.model.Message;
import adam.brooks.social.service.CallLogService;
import adam.brooks.social.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final CallLogService callLogService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummary>> getConversations(Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(chatService.getConversationsForUser(currentUserId));
    }

    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<Message>> getDirectHistory(@PathVariable String otherUserId,
                                                           Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(chatService.getDirectHistory(currentUserId, otherUserId, currentUserId));
    }

    // past voice/video calls with this person, newest first
    @GetMapping("/call-history/{otherUserId}")
    public ResponseEntity<List<CallLog>> getCallHistory(@PathVariable String otherUserId,
                                                         Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(callLogService.getCallHistory(currentUserId, otherUserId));
    }

    @GetMapping("/group/{groupId}/history")
    public ResponseEntity<List<Message>> getGroupHistory(@PathVariable String groupId,
                                                          Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(chatService.getGroupHistory(groupId, currentUserId));
    }

    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        chatService.deleteMessageForMe(messageId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/conversation/{otherUserId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String otherUserId, Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        chatService.deleteConversationForMe(otherUserId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
