package com.example.ai_english.domain.feedback.dto.response;

import com.example.ai_english.domain.feedback.entity.FeedbackReport;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public record FeedbackResponse(String summary, String strengths, Integer overallScore, Integer grammarScore,
                               Integer vocabularyScore, Integer pronunciationScore, Integer fluencyScore,
                               List<FeedbackErrorResponse> errors, List<FeedbackVocabularyResponse> vocabularies) {
    public record FeedbackErrorResponse(String category, String original, String suggestion, String explanation,
                                        String example, String advice) {
    }

    public record FeedbackVocabularyResponse(String word, String meaning, String exampleSentence,
                                             Boolean isRecommended) {
    }

    public static FeedbackResponse from(FeedbackReport report) {
        return FeedbackResponse.builder()
                .summary(report.getSummary())
                .strengths(report.getStrengths())
                .overallScore(report.getOverallScore())
                .grammarScore(report.getGrammarScore())
                .vocabularyScore(report.getVocabularyScore())
                .pronunciationScore(report.getPronunciationScore())
                .fluencyScore(report.getFluencyScore())
                .errors(report.getErrors().stream()
                        .map(e -> new FeedbackErrorResponse(
                                e.getCategory().name(),
                                e.getOriginal(),
                                e.getSuggestion(),
                                e.getExplanation(),
                                e.getExample(),
                                e.getAdvice()))
                        .collect(Collectors.toList()))
                .vocabularies(report.getVocabularies().stream()
                        .map(v -> new FeedbackVocabularyResponse(
                                v.getWord(),
                                v.getMeaning(),
                                v.getExampleSentence(),
                                v.getIsRecommended()))
                        .collect(Collectors.toList()))
                .build();
    }
}
