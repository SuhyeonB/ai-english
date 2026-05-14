package com.example.ai_english.domain.conversation.repository;

import com.example.ai_english.domain.conversation.entity.ConversationMessage;
import com.example.ai_english.domain.conversation.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    List<ConversationMessage> findBySession(ConversationSession session);
}
