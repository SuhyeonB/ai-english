package com.example.ai_english.domain.conversation.repository;

import com.example.ai_english.domain.conversation.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
}
