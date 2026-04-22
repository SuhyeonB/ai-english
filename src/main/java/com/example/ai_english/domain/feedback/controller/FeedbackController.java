package com.example.ai_english.domain.feedback.controller;

import com.example.ai_english.domain.auth.dto.CustomOAuth2User;
import com.example.ai_english.domain.feedback.dto.response.FeedbackResponse;
import com.example.ai_english.domain.feedback.service.FeedbackService;
import com.example.ai_english.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback (
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Long feedbackId
            ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(feedbackService.findFeedback(principal.getUserId(), feedbackId)));
    }
}
