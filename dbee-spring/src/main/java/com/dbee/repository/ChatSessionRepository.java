package com.dbee.repository;

import com.dbee.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);
    @Modifying
    @Query("delete from ChatSession session where session.user.id = :userId")
    int deleteAllOwnedByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from ChatSession session where session.id = :sessionId and session.user.id = :userId")
    int deleteOwnedSession(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
