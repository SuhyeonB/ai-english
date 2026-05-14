package com.example.ai_english.domain.user.dto.response;

import com.example.ai_english.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserResponse(String nickname, String email, int streakDays, int sessionCount) {

    public static UserResponse from(User user, int sessionCount) {
        return UserResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .streakDays(user.getStreakDays())
                .sessionCount(sessionCount)
                .build();
    }
}
