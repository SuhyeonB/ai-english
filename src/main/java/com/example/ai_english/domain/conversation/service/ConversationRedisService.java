package com.example.ai_english.domain.conversation.service;

import com.example.ai_english.domain.conversation.dto.ChatMessage;
import com.example.ai_english.global.exception.BusinessException;
import com.example.ai_english.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ConversationRedisService {

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 세션 초기화 (빈 메세지 리스트)
    public void initSession(Long sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, "[]", SESSION_TTL);
    }

    public void addMessage(Long sessionId, ChatMessage message) {
        String key = SESSION_KEY_PREFIX + sessionId;

        List<ChatMessage> messages = getMessages(sessionId);
        messages.add(message);

        try {
            String json = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForValue().set(key, json, SESSION_TTL);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    public List<ChatMessage> getMessages(Long sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String raw = redisTemplate.opsForValue().get(key);

        if (raw == null) return new ArrayList<>();

        try {
            return objectMapper.readValue(
                    raw,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ChatMessage.class)
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    public boolean existsSession(Long sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        return redisTemplate.hasKey(key);
    }

    public void deleteSession(Long sessionId) { redisTemplate.delete(SESSION_KEY_PREFIX + sessionId); }
}
