package com.example.ai_english.domain.conversation.dto.response;

import com.example.ai_english.domain.conversation.entity.ConversationMessage;
import com.example.ai_english.domain.conversation.entity.ConversationSession;

import java.util.List;

public record SessionDetailResponse(Long sessionId, List<SessionMessage> messages) {
    public record SessionMessage(String role, String content) {
        public static SessionMessage from(ConversationMessage message) {
            return new SessionMessage(
                    message.getRole().name(),
                    message.getContent()
            );
        }
    }

    public static SessionDetailResponse from(ConversationSession session) {
        return new SessionDetailResponse(
                session.getId(),
                session.getMessages().stream()
                        .map(SessionMessage::from)
                        .toList()
        );
    }
}
