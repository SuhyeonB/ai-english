package com.example.ai_english.domain.conversation.repository;

import com.example.ai_english.domain.conversation.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

}
