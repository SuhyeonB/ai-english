package com.example.ai_english.domain.user.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ProgressResponse (List<DailyProgress> weekly){
    public record DailyProgress(LocalDate date, Double score) {
        public static DailyProgress from(Object[] row) {
            return new DailyProgress(
                    ((java.sql.Date) row[0]).toLocalDate(),
                    (Double) row[1]
            );
        }
    }
}
