package com.dbee.controller;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Size;

public final class ChatDtos {
    private ChatDtos() {}

    public record SessionRequest(@Size(max = 160) String title) {}
    public record SessionResponse(Long id, String title, Instant createdAt) {}
    public record SessionEnvelope(SessionResponse session) {}
    public record SessionsEnvelope(List<SessionResponse> sessions) {}
    public record MessageRequest(@Size(min = 1, max = 4000) String message, @Size(min = 1, max = 4000) String query) {}
    public record LegacyQueryRequest(Long sessionId, String message, String query) {}
    public record MessageResponse(Long id, String sender, String content, Instant createdAt) {}
    public record MessagesEnvelope(List<MessageResponse> messages) {}
    public record ReplyResponse(String reply) {}
}
