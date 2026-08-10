package adam.brooks.social.controller;

import adam.brooks.social.dto.CallSignal;
import adam.brooks.social.dto.ChatDtos.DirectMessageRequest;
import adam.brooks.social.dto.ChatDtos.GroupMessageRequest;
import adam.brooks.social.dto.ChatDtos.MessagePayload;
import adam.brooks.social.model.Message;
import adam.brooks.social.service.CallLogService;
import adam.brooks.social.service.ChatService;
import adam.brooks.social.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final UserService userService;
    private final CallLogService callLogService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendDirectMessage(DirectMessageRequest req, Principal principal) {
        String senderId = principal.getName();

        try {
            Message saved = chatService.sendDirectMessage(senderId, req.getReceiverId(), req.getContent());
            MessagePayload payload = toPayload(saved, senderId);

            messagingTemplate.convertAndSendToUser(req.getReceiverId(), "/queue/messages", payload);
            messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", payload);
        } catch (SecurityException e) {
            messagingTemplate.convertAndSendToUser(senderId, "/queue/errors", Map.of("error", e.getMessage()));
        }
    }

    @MessageMapping("/chat.group.send")
    public void sendGroupMessage(GroupMessageRequest req, Principal principal) {
        String senderId = principal.getName();

        Message saved = chatService.sendGroupMessage(senderId, req.getGroupId(), req.getContent());
        MessagePayload payload = toPayload(saved, senderId);

        messagingTemplate.convertAndSend("/topic/group." + req.getGroupId(), payload);
    }

    @MessageMapping("/call.signal")
    public void handleCallSignal(CallSignal signal, Principal principal) {
        String senderId = principal.getName();
        signal.setFromUserId(senderId);
        String otherUserId = signal.getToUserId();

        if (userService.isBlockedEitherWay(senderId, otherUserId)) {
            return;
        }

        switch (signal.getType()) {
            case "OFFER" -> callLogService.callStarted(senderId, otherUserId, signal.getCallType());
            case "ANSWER" -> callLogService.callAnswered(senderId, otherUserId);
            case "CALL_DECLINE" -> callLogService.callDeclined(senderId, otherUserId);
            case "CALL_END" -> callLogService.callEnded(senderId, otherUserId);
            default -> { /* ICE_CANDIDATE and anything else — just relay, nothing to log */ }
        }

        messagingTemplate.convertAndSendToUser(otherUserId, "/queue/call", signal);
    }

    private MessagePayload toPayload(Message message, String senderId) {
        MessagePayload payload = new MessagePayload();
        payload.setId(message.getId());
        payload.setSenderId(senderId);
        payload.setSenderName(userService.getById(senderId).getUsername());
        payload.setReceiverId(message.getReceiverId());
        payload.setGroupId(message.getGroupId());
        payload.setContent(message.getContent());
        payload.setType(message.getType());
        payload.setCreatedAt(message.getCreatedAt().toString());
        return payload;
    }
}
