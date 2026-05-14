package com.example.ai_english.domain.conversation.repository;

import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    int countByUser(User user);

    @Query("SELECT COUNT(s), SUM(s.durationSeconds) "+
            "FROM ConversationSession s " +
            "WHERE s.user = :user AND s.status = 'COMPLETED'")
    Object[] findStatsByUser (@Param("user") User user);

    List<ConversationSession> findByUserOrderByStartedAtDesc(User user);

    List<ConversationSession> findTop10ByUserOrderByStartedAtDesc(User user);
}
