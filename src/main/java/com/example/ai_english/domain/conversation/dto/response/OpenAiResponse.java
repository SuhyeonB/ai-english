package com.example.ai_english.domain.conversation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiResponse {

    private List<Choice> choices;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;   // non-stream
        private Delta delta;       // stream
        private String finish_reason; // 응답 종료 이유
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        private String role;
        private String content;
    }
}
