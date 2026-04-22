package com.example.ai_english.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "feedback_vocabularies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private FeedbackReport feedbackReport;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(columnDefinition = "TEXT")
    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(nullable = false)
    private Boolean isRecommended = false;

    @Builder
    public FeedbackVocabulary(FeedbackReport feedbackReport, String word, String meaning,
                              String exampleSentence, Boolean isRecommended) {
        this.feedbackReport = feedbackReport;
        this.word = word;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
        this.isRecommended = isRecommended;
    }
}