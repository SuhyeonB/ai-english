package com.example.ai_english.domain.user.dto.response;

import com.example.ai_english.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UpdateUserResponse (String nickname, String email){

    public static UpdateUserResponse from(User user) {
        return UpdateUserResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }
}
