package com.example.ai_english.domain.batch.dto;

public record WeeklyReportDto(Long userId, Long attendanceDays, Double avgScore, Long sessionCount, String weakness) {
}
