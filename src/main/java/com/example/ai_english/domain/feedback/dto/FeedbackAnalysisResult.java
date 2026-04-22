package com.example.ai_english.domain.feedback.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackAnalysisResult {

    private String summary;
    private String strengths;

    @JsonProperty("overall_score")
    private Integer overallScore;

    @JsonProperty("grammar_score")
    private Integer grammarScore;

    @JsonProperty("vocabulary_score")
    private Integer vocabularyScore;

    @JsonProperty("pronunciation_score")
    private Integer pronunciationScore;

    @JsonProperty("fluency_score")
    private Integer fluencyScore;

    private List<ErrorDto> errors;
    private List<VocabularyDto> vocabularies;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDto {

        private String category;
        private String original;
        private String suggestion;
        private String explanation;
        private String example;
        private String advice;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VocabularyDto {

        private String word;
        private String meaning;
        private String exampleSentence;

        private Boolean isRecommended;
    }
}