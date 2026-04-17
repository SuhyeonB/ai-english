package com.example.ai_english.domain.conversation.service;

import com.example.ai_english.domain.conversation.dto.ChatMessage;
import com.example.ai_english.domain.conversation.dto.request.SendMessageRequest;
import com.example.ai_english.domain.conversation.dto.response.CreateSessionResponse;
import com.example.ai_english.domain.conversation.entity.ConversationMessage;
import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.conversation.entity.MessageRole;
import com.example.ai_english.domain.conversation.entity.Status;
import com.example.ai_english.domain.conversation.repository.ConversationMessageRepository;
import com.example.ai_english.domain.conversation.repository.ConversationSessionRepository;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.service.UserService;
import com.example.ai_english.global.exception.BusinessException;
import com.example.ai_english.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationRedisService conversationRedisService;
    private final OpenAiService openAiService;
    private final UserService userService;

    @Transactional
    public CreateSessionResponse createSession(Long userId) {
        User user = userService.findUser(userId);

        ConversationSession session = ConversationSession.builder().user(user).build();
        conversationSessionRepository.save(session);

        conversationRedisService.initSession(session.getId());

        return CreateSessionResponse.from(session);
    }

    public Flux<String> sendMessage(Long userId, Long sessionId, SendMessageRequest dto) {
        ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus().equals(Status.COMPLETED)) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_ENDED);
        }
        if (!session.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_SESSION_ACCESS);
        }

        // conversation_messages 테이블에 클라이언트 메세지(user) 저장
        saveUserMessage(sessionId, dto.getContent());

        // redis에 추가(addMessages)
        ChatMessage chatMessage = ChatMessage.ofUser(dto.getContent());
        conversationRedisService.addMessage(sessionId, chatMessage);

        // OpenAI에 system + 히스토리 + userMessage 전달
        // OpenAI 답변
        List<ChatMessage> history = conversationRedisService.getMessages(sessionId);

        StringBuilder fullResponse = new StringBuilder();

        // 답변을 클라이언트에 전달 (stream(Flux))
        // conversation_messages 테이블에 서버 메세지(assistant) 저장
        return openAiService.stream(history, dto.getContent())
                .doOnNext(fullResponse::append)
                .doOnComplete(() ->  {
                    saveAssistantMessage(sessionId, fullResponse.toString());
                    conversationRedisService.addMessage(sessionId, ChatMessage.ofAssistant(fullResponse.toString()));
                });
    }

    @Transactional
    public void endSession(Long userId, Long sessionId) {
        ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus().equals(Status.COMPLETED)) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_ENDED);
        }
        if (!session.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_SESSION_ACCESS);
        }

        // 1. 세션 상태 변경
        session.end();

        // 2. 피드백 분석은 비동기로 시작

        // 3. feedbackId 즉시 반환

        // redis 정리
        conversationRedisService.deleteSession(sessionId);
    }

    @Transactional
    public void saveUserMessage(Long sessionId, String userMessage) {
        ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
        ConversationMessage message = ConversationMessage.builder()
                .session(session)
                .role(MessageRole.USER)
                .content(userMessage)
                .tokenCount(0)
                .build();
        conversationMessageRepository.save(message);
        session.increaseCount();
    }

    @Transactional
    public void saveAssistantMessage(Long sessionId, String fullResponse) {
        ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
        ConversationMessage message = ConversationMessage.builder()
                .session(session)
                .role(MessageRole.ASSISTANT)
                .content(fullResponse)
                .tokenCount(0)
                .build();
        conversationMessageRepository.save(message);
        session.increaseCount();
    }
}
