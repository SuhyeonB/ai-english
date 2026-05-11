package com.example.ai_english.domain.user.dto.response;

import com.example.ai_english.domain.conversation.entity.ConversationSession;

import java.time.LocalDateTime;
import java.util.List;

public record HomeStatsResponse(Long totalSessions, Long totalMinutes, Double avgScore, Integer streakDays,
                                List<SessionSummary> recentSessions) {
    public record SessionSummary(Long sessionId, LocalDateTime startedAt, Integer durationSeconds, String status) {
        public static SessionSummary from(ConversationSession session) {
            return new SessionSummary(
                    session.getId(),
                    session.getStartedAt(),
                    session.getDurationSeconds(),
                    session.getStatus().name()
            );
        }
    }
}
