package com.example.ai_english.domain.conversation.dto.response;

import com.example.ai_english.domain.conversation.entity.ConversationSession;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateSessionResponse(Long sessionId, LocalDateTime startedAt) {

    public static CreateSessionResponse from(ConversationSession session) {
        return CreateSessionResponse.builder()
                .sessionId(session.getId())
                .startedAt(session.getStartedAt())
                .build();
    }
}
