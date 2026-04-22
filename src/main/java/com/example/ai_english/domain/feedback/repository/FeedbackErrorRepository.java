package com.example.ai_english.domain.feedback.repository;

import com.example.ai_english.domain.feedback.entity.FeedbackError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackErrorRepository extends JpaRepository<FeedbackError, Long> {
}
