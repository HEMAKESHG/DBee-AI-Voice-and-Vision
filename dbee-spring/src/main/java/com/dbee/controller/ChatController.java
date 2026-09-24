package com.dbee.controller;

import com.dbee.model.ChatSession;
import com.dbee.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) { this.chatService = chatService; }

    @GetMapping("/sessions")
    public ChatDtos.SessionsEnvelope sessions(@AuthenticationPrincipal Long userId) {
        return new ChatDtos.SessionsEnvelope(chatService.listSessions(userId));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatDtos.SessionEnvelope> createSession(@AuthenticationPrincipal Long userId,
                                                                    @Valid @RequestBody(required = false) ChatDtos.SessionRequest request) {
        ChatSession session = chatService.createSession(userId, request);
        return ResponseEntity.status(201).body(new ChatDtos.SessionEnvelope(
                new ChatDtos.SessionResponse(session.getId(), session.getTitle(), session.getCreatedAt())));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ChatDtos.MessagesEnvelope messages(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        return new ChatDtos.MessagesEnvelope(chatService.messages(userId, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ChatDtos.ReplyResponse send(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId,
                                       @Valid @RequestBody ChatDtos.MessageRequest request) {
        String content = request.message() != null ? request.message().trim() : request.query() == null ? "" : request.query().trim();
        if (content.isBlank() || content.length() > 4000) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "A message of 1-4000 characters is required.");
        }
        return new ChatDtos.ReplyResponse(chatService.processMessage(userId, sessionId, content));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> deleteAllSessions(@AuthenticationPrincipal Long userId) {
        chatService.deleteAllSessions(userId);
        return ResponseEntity.noContent().build();
    }

}
