package com.example.ai_english.domain.feedback.service;

import com.example.ai_english.domain.conversation.dto.ChatMessage;
import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.conversation.repository.ConversationSessionRepository;
import com.example.ai_english.domain.conversation.service.ConversationRedisService;
import com.example.ai_english.domain.conversation.service.ConversationService;
import com.example.ai_english.domain.conversation.service.OpenAiService;
import com.example.ai_english.domain.feedback.dto.FeedbackAnalysisResult;
import com.example.ai_english.domain.feedback.dto.response.FeedbackResponse;
import com.example.ai_english.domain.feedback.entity.Category;
import com.example.ai_english.domain.feedback.entity.FeedbackError;
import com.example.ai_english.domain.feedback.entity.FeedbackReport;
import com.example.ai_english.domain.feedback.entity.FeedbackVocabulary;
import com.example.ai_english.domain.feedback.repository.FeedbackErrorRepository;
import com.example.ai_english.domain.feedback.repository.FeedbackReportRepository;
import com.example.ai_english.domain.feedback.repository.FeedbackVocabularyRepository;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.service.UserService;
import com.example.ai_english.global.exception.BusinessException;
import com.example.ai_english.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackReportRepository feedbackReportRepository;
    private final FeedbackErrorRepository feedbackErrorRepository;
    private final FeedbackVocabularyRepository feedbackVocabularyRepository;
    private final ConversationSessionRepository conversationSessionRepository;

    private final ConversationRedisService conversationRedisService;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void createFeedbackAsync(Long userId, Long sessionId) {
        ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_SESSION_ACCESS);
        }

        List<ChatMessage> history = conversationRedisService.getMessages(sessionId);

        String response = openAiService.chat(history);

        FeedbackAnalysisResult result = null;
        try {
            result = objectMapper.readValue(response, FeedbackAnalysisResult.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR);
        }

        FeedbackReport feedbackReport = FeedbackReport.builder()
                .session(session)
                .summary(result.getSummary())
                .strengths(result.getStrengths())
                .overallScore(result.getOverallScore())
                .grammarScore(result.getGrammarScore())
                .vocabularyScore(result.getVocabularyScore())
                .pronunciationScore(result.getPronunciationScore())
                .fluencyScore(result.getFluencyScore())
                .build();
        feedbackReportRepository.save(feedbackReport);

        for (FeedbackAnalysisResult.ErrorDto errorDto : result.getErrors()) {
            FeedbackError error = FeedbackError.builder()
                    .feedbackReport(feedbackReport)
                    .category(Category.valueOf(errorDto.getCategory()))
                    .original(errorDto.getOriginal())
                    .suggestion(errorDto.getSuggestion())
                    .explanation(errorDto.getExplanation())
                    .example(errorDto.getExample())
                    .advice(errorDto.getAdvice())
                    .build();
            feedbackErrorRepository.save(error);
        }

        for (FeedbackAnalysisResult.VocabularyDto vocaDto : result.getVocabularies()) {
            FeedbackVocabulary vocabulary = FeedbackVocabulary.builder()
                    .feedbackReport(feedbackReport)
                    .word(vocaDto.getWord())
                    .meaning(vocaDto.getMeaning())
                    .exampleSentence(vocaDto.getExampleSentence())
                    .isRecommended(vocaDto.getIsRecommended())
                    .build();
            feedbackVocabularyRepository.save(vocabulary);
        }
    }

    @Transactional(readOnly = true)
    public FeedbackResponse findFeedback(Long userId, Long feedbackId) {
        FeedbackReport feedbackReport = feedbackReportRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEEDBACK_NOT_FOUND));

        if (!feedbackReport.getSession().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_SESSION_ACCESS);
        }

        return FeedbackResponse.from(feedbackReport);
    }
}
