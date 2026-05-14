package com.example.ai_english.domain.conversation.service;

import com.example.ai_english.domain.conversation.dto.ChatMessage;
import com.example.ai_english.domain.conversation.dto.request.SendMessageRequest;
import com.example.ai_english.domain.conversation.dto.response.CreateSessionResponse;
import com.example.ai_english.domain.conversation.dto.response.SessionDetailResponse;
import com.example.ai_english.domain.conversation.dto.response.SessionResponse;
import com.example.ai_english.domain.conversation.entity.ConversationMessage;
import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.conversation.entity.MessageRole;
import com.example.ai_english.domain.conversation.entity.Status;
import com.example.ai_english.domain.conversation.repository.ConversationMessageRepository;
import com.example.ai_english.domain.conversation.repository.ConversationSessionRepository;
import com.example.ai_english.domain.feedback.service.FeedbackService;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.service.UserService;
import com.example.ai_english.global.exception.BusinessException;
import com.example.ai_english.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationRedisService conversationRedisService;
    private final OpenAiService openAiService;
    private final UserService userService;
    private final FeedbackService feedbackService;

    @Transactional
    public CreateSessionResponse createSession(Long userId) {
        User user = userService.findUser(userId);

        ConversationSession session = ConversationSession.builder().user(user).build();
        conversationSessionRepository.save(session);

        conversationRedisService.initSession(session.getId());

        // 출석
        user.attend();

        return CreateSessionResponse.from(session);
    }

    public Flux<String> sendMessage(Long userId, Long sessionId, SendMessageRequest dto) {
        ConversationSession session = findSession(sessionId);

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
                .subscribeOn(Schedulers.boundedElastic()) // 전체를 blocking-safe 스레드로
                .doOnNext(fullResponse::append)            // 응답 누적
                .concatWith(Mono.fromRunnable(() -> {     // 트림 종료 후 실행
                    String finalResponse = fullResponse.toString();

                    saveAssistantMessage(sessionId, finalResponse);
                    conversationRedisService.addMessage(
                            sessionId,
                            ChatMessage.ofAssistant(finalResponse)
                    );
                }))
                .onErrorResume(e -> {
                    return Flux.just("error: " + e.getMessage());
                });
    }

    @Transactional
    public void endSession(Long userId, Long sessionId) {
        ConversationSession session = findSession(sessionId);

        if (session.getStatus().equals(Status.COMPLETED)) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_ENDED);
        }
        if (!session.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_SESSION_ACCESS);
        }

        // 1. 세션 상태 변경
        session.end();

        // 2. 피드백 분석은 비동기로 시작
        feedbackService.createFeedbackAsync(userId, sessionId);

        // redis 정리
        conversationRedisService.deleteSession(sessionId);
    }

    @Transactional
    public void saveUserMessage(Long sessionId, String userMessage) {
        ConversationSession session = findSession(sessionId);
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
        ConversationSession session = findSession(sessionId);
        ConversationMessage message = ConversationMessage.builder()
                .session(session)
                .role(MessageRole.ASSISTANT)
                .content(fullResponse)
                .tokenCount(0)
                .build();
        conversationMessageRepository.save(message);
        session.increaseCount();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionList (Long userId) {
        User user = userService.findUser(userId);

        List<ConversationSession> sessions = conversationSessionRepository.findByUserOrderByStartedAtDesc(user);

        return sessions.stream().map(SessionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getDetailSession (Long userId, Long sessionId) {
        userService.findUser(userId);
        ConversationSession session = findSession(sessionId);

        if (!session.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_SESSION_ACCESS);
        }
        List<ConversationMessage> messages = conversationMessageRepository.findBySession(session);

        return new SessionDetailResponse(sessionId, messages.stream().map(SessionDetailResponse.SessionMessage::from).toList());
    }

    public ConversationSession findSession(Long sessionId) {
        return conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }
}
