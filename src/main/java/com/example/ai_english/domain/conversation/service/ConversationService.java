package com.example.ai_english.domain.conversation.service;

import com.example.ai_english.domain.conversation.dto.response.CreateSessionResponse;
import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.conversation.repository.ConversationSessionRepository;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ConversationRedisService conversationRedisService;
    private final UserService userService;

    @Transactional
    public CreateSessionResponse createSession(Long userId) {
        User user = userService.findUser(userId);

        ConversationSession session = ConversationSession.builder().user(user).build();
        conversationSessionRepository.save(session);

        conversationRedisService.initSession(session.getId());

        return CreateSessionResponse.from(session);
    }
}
