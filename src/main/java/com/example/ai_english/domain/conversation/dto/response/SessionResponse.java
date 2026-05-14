package com.example.ai_english.domain.conversation.dto.response;

import com.example.ai_english.domain.conversation.entity.ConversationSession;

import java.time.LocalDateTime;

public record SessionResponse (Long sessionId, LocalDateTime startedAt, Integer duration, Integer messageCount) {
    public static SessionResponse from (ConversationSession session) {
        return new SessionResponse(
                session.getId(),
                session.getStartedAt(),
                session.getDurationSeconds(),
                session.getMessageCount()
        );
    }
}
