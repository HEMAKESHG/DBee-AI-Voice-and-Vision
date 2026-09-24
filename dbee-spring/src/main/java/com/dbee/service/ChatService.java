package com.dbee.service;

import com.dbee.controller.ChatDtos;
import com.dbee.model.ChatMessage;
import com.dbee.model.ChatSession;
import com.dbee.model.User;
import com.dbee.controller.ApiException;
import com.dbee.repository.ChatMessageRepository;
import com.dbee.repository.ChatSessionRepository;
import com.dbee.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DialogflowService dialogflowService;

    public ChatService(ChatSessionRepository sessionRepository, ChatMessageRepository messageRepository,
                       UserRepository userRepository, DialogflowService dialogflowService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.dialogflowService = dialogflowService;
    }

    public List<ChatDtos.SessionResponse> listSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toSession).toList();
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        if (sessionRepository.deleteOwnedSession(sessionId, userId) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Chat not found.");
        }
    }

    @Transactional
    public void deleteAllSessions(Long userId) {
        sessionRepository.deleteAllOwnedByUserId(userId);
    }

    @Transactional
    public ChatSession createSession(Long userId, ChatDtos.SessionRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required."));
        ChatSession session = new ChatSession();
        session.setUser(user);
        String title = request == null || request.title() == null || request.title().isBlank() ? "New chat" : request.title().trim();
        session.setTitle(title.substring(0, Math.min(160, title.length())));
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<ChatDtos.MessageResponse> messages(Long userId, Long sessionId) {
        ChatSession session = ownedSession(userId, sessionId);
        return session.getMessages().stream().map(this::toMessage).toList();
    }

    @Transactional
    public String processMessage(Long userId, Long sessionId, String content) {
        ChatSession session = ownedSession(userId, sessionId);
        if ("New chat".equals(session.getTitle())) {
            session.setTitle(content.substring(0, Math.min(160, content.length())));
            sessionRepository.save(session);
        }
        ChatMessage userMessage = newMessage(session, ChatMessage.Sender.user, content);
        messageRepository.save(userMessage);
        String reply = dialogflowService.detectReply(content);
        messageRepository.save(newMessage(session, ChatMessage.Sender.bot, reply));
        return reply;
    }

    private ChatSession ownedSession(Long userId, Long sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chat not found."));
    }

    private ChatMessage newMessage(ChatSession session, ChatMessage.Sender sender, String content) {
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender(sender);
        message.setContent(content);
        return message;
    }

    private ChatDtos.SessionResponse toSession(ChatSession session) {
        return new ChatDtos.SessionResponse(session.getId(), session.getTitle(), session.getCreatedAt());
    }

    private ChatDtos.MessageResponse toMessage(ChatMessage message) {
        return new ChatDtos.MessageResponse(message.getId(), message.getSender().name(), message.getContent(), message.getCreatedAt());
    }
}
