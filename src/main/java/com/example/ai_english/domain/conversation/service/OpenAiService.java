package com.example.ai_english.domain.conversation.service;

import com.example.ai_english.global.constant.PromptConstants;
import com.example.ai_english.domain.conversation.dto.ChatMessage;
import com.example.ai_english.domain.conversation.dto.response.OpenAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient openAiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.stream-max-tokens}")
    private int streamMaxTokens;

    @Value("${openai.chat-max-tokens}")
    private int chatMaxTokens;

    public String chat(List<ChatMessage> history) {
        List<ChatMessage> messages = buildMessages(PromptConstants.FEEDBACK_PROMPT, history, "");

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", chatMaxTokens,
                "messages", messages
        );

        OpenAiResponse response = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAiResponse.class)
                .block();

        return extractContent(response);
    }

    public Flux<String> stream(List<ChatMessage> conversationHistory, String userMessage) {
        List<ChatMessage> messages = buildMessages(PromptConstants.CHAT_PROMPT, conversationHistory, userMessage);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", streamMaxTokens,
                "stream", true,
                "messages", messages
        );

        return openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> log.info("chunk: {}", chunk))
                .doOnNext(chunk -> log.info("parsed chunk: {}", chunk))
                .filter(chunk -> !chunk.equals("[DONE]"))
                .mapNotNull(this::extractStreamContext)
                .doOnNext(chunk -> log.info("parsed chunk: {}", chunk));

    }

    public List<ChatMessage> buildMessages(String systemPrompt, List<ChatMessage> history, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();

        // 1. system prompt
        messages.add(ChatMessage.ofSystem(systemPrompt));

        // 2. history
        messages.addAll(history);

        // 3. new user message
        if (!userMessage.isEmpty()) {
            messages.add(ChatMessage.ofUser(userMessage));
        }

        return messages;
    }

    private String extractContent(OpenAiResponse response) {
        return response.getChoices().get(0).getMessage().getContent();
    }

    private String extractStreamContext(String chunk) {
        try {
            OpenAiResponse response = objectMapper.readValue(chunk, OpenAiResponse.class);

            if (response.getChoices() == null || response.getChoices().isEmpty()) {
                return null;
            }

            OpenAiResponse.Delta delta = response.getChoices().get(0).getDelta();
            return delta != null ? delta.getContent() : null;

        } catch (Exception e) {
            return null;
        }
    }
}
