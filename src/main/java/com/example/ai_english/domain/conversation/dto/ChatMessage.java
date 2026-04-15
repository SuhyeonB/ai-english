package com.example.ai_english.domain.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String role;        // "user" | "assistant" | "system"
    private String content;

    public static ChatMessage ofUser(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage ofAssistant(String content) { return new ChatMessage("assistant", content); }

    public static ChatMessage ofSystem(String content) { return new ChatMessage("system", content); }
}
