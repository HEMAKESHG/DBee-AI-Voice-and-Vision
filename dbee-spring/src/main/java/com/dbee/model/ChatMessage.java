package com.dbee.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = @Index(name = "idx_chat_messages_session_created", columnList = "session_id, created_at"))
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_messages_session"))
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private Sender sender;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public ChatSession getSession() { return session; }
    public Sender getSender() { return sender; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setSession(ChatSession session) { this.session = session; }
    public void setSender(Sender sender) { this.sender = sender; }
    public void setContent(String content) { this.content = content; }

    public enum Sender { user, bot }
}
