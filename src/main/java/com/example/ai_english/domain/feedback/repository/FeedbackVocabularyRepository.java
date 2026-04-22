package com.example.ai_english.domain.feedback.repository;

import com.example.ai_english.domain.feedback.entity.FeedbackVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackVocabularyRepository extends JpaRepository<FeedbackVocabulary, Long> {
}
