package com.example.ai_english.domain.conversation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "메시지 내용은 비워둘 수 없습니다.")
    private String content;
}
