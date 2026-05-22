package com.example.ai_english.domain.batch.dto;

public record WeeklyReportRawDto(Long userId, Double avgScore,
                                 Double grammarScore, Double vocabularyScore, Double pronunciationScore, Double fluencyScore,
                                 Long sessionCount) {
}
