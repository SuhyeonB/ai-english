package com.example.ai_english.domain.conversation.controller;

import com.example.ai_english.domain.auth.dto.CustomOAuth2User;
import com.example.ai_english.domain.conversation.dto.request.SendMessageRequest;
import com.example.ai_english.domain.conversation.dto.response.CreateSessionResponse;
import com.example.ai_english.domain.conversation.service.ConversationService;
import com.example.ai_english.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateSessionResponse>> createSession(
            @AuthenticationPrincipal CustomOAuth2User principal
            ) {
        CreateSessionResponse response = conversationService.createSession(principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping(value = "/{sessionId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Long sessionId,
            @RequestBody SendMessageRequest dto
            ) {
        return conversationService.sendMessage(principal.getUserId(), sessionId, dto);
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ApiResponse<Void>> endSession(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Long sessionId
            ) {
        conversationService.endSession(principal.getUserId(), sessionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success());
    }
}
