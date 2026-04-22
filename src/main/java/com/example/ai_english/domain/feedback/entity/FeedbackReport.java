package com.example.ai_english.domain.feedback.entity;

import com.example.ai_english.domain.conversation.entity.ConversationSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feedback_reports")
public class FeedbackReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ConversationSession session;

    private Integer overallScore;
    private Integer grammarScore;
    private Integer vocabularyScore;
    private Integer pronunciationScore;
    private Integer fluencyScore;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @OneToMany(mappedBy = "feedbackReport", fetch = FetchType.LAZY)
    private List<FeedbackError> errors = new ArrayList<>();

    @OneToMany(mappedBy = "feedbackReport", fetch = FetchType.LAZY)
    private List<FeedbackVocabulary> vocabularies = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Builder
    public FeedbackReport(ConversationSession session,
                          int overallScore, int grammarScore, int vocabularyScore, int pronunciationScore, int fluencyScore,
                          String summary, String strengths) {
        this.session = session;
        this.overallScore = overallScore;
        this.grammarScore = grammarScore;
        this.vocabularyScore = vocabularyScore;
        this.pronunciationScore = pronunciationScore;
        this.fluencyScore = fluencyScore;
        this.summary = summary;
        this.strengths = strengths;
    }
}
