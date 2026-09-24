package com.dbee.controller;

import com.dbee.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/query")
public class LegacyQueryController {
    private final ChatService chatService;

    public LegacyQueryController(ChatService chatService) { this.chatService = chatService; }

    @PostMapping
    public ChatDtos.ReplyResponse query(@AuthenticationPrincipal Long userId, @RequestBody ChatDtos.LegacyQueryRequest request) {
        String content = request.message() != null ? request.message().trim() : request.query() == null ? "" : request.query().trim();
        if (request.sessionId() == null || content.isBlank() || content.length() > 4000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "sessionId and a message of 1-4000 characters are required.");
        }
        return new ChatDtos.ReplyResponse(chatService.processMessage(userId, request.sessionId(), content));
    }
}