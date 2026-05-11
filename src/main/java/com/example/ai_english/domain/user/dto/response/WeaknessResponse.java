package com.example.ai_english.domain.user.dto.response;

import com.example.ai_english.domain.feedback.entity.FeedbackError;
import com.example.ai_english.domain.user.entity.UserWeaknessStat;
import com.example.ai_english.global.entity.Category;

import java.util.List;

public record WeaknessResponse(
        List<Distribution> distributions,
        List<Mistake> top5Mistakes
) {
    public record Distribution(
            String category,
            int count,
            double percentage
    ) {
        public static Distribution of(Category category, int count, int total) {
            double percentage =
                    total == 0 ? 0 : (double) count / total;

            return new Distribution(
                    category.name(),
                    count,
                    percentage
            );
        }
    }

    public record Mistake(
            String category,
            String tag,
            int count,
            String exampleOriginal,
            String exampleSuggestion
    ) {
        public static Mistake of(
                UserWeaknessStat stat,
                FeedbackError error
        ) {
            return new Mistake(
                    stat.getCategory().name(),
                    stat.getTag(),
                    stat.getCount(),
                    error != null ? error.getOriginal() : null,
                    error != null ? error.getSuggestion() : null
            );
        }
    }
}
