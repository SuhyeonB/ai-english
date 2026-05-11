package com.example.ai_english.domain.feedback.repository;

import com.example.ai_english.domain.feedback.entity.FeedbackError;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.global.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackErrorRepository extends JpaRepository<FeedbackError, Long> {
    Optional<FeedbackError> findTopByFeedbackReport_Session_UserAndCategoryAndTagOrderByIdDesc(
            User user, Category category, String tag);
}
