package com.example.ai_english.domain.user.dto.response;

import com.example.ai_english.domain.user.entity.UserGoal;

import java.time.LocalDate;

public record GoalResponse(String goalType, int targetValue, int currentValue, LocalDate startDate, LocalDate endDate, Boolean isAchieved) {
    public static GoalResponse from(UserGoal goal) {
        return new GoalResponse(
                goal.getGoalType(),
                goal.getTargetValue(),
                goal.getCurrentValue(),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.getIsAchieved()
        );
    }
}