package com.example.ai_english.domain.feedback.repository;

import com.example.ai_english.domain.feedback.entity.FeedbackReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackReportRepository extends JpaRepository<FeedbackReport, Long> {
}
