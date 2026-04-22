package com.example.ai_english.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "feedback_errors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private FeedbackReport feedbackReport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String original;

    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Builder
    public FeedbackError(FeedbackReport feedbackReport, Category category, String original, String suggestion,
                         String explanation, String example, String advice) {
        this.feedbackReport = feedbackReport;
        this.category = category;
        this.original = original;
        this.suggestion = suggestion;
        this.explanation = explanation;
        this.example = example;
        this.advice = advice;
    }
}
