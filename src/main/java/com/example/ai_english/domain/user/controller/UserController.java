package com.example.ai_english.domain.user.controller;

import com.example.ai_english.domain.auth.dto.CustomOAuth2User;
import com.example.ai_english.domain.user.dto.request.UpdateUserRequest;
import com.example.ai_english.domain.user.dto.response.*;
import com.example.ai_english.domain.user.service.UserService;
import com.example.ai_english.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(userService.getProfile(principal.getUserId())));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateProfile(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestBody UpdateUserRequest dto
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(userService.updateProfile(principal.getUserId(), dto)));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<HomeStatsResponse>> getUserStats(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(userService.getUserStats(principal.getUserId())));
    }

    @GetMapping("/me/weakness")
    public ResponseEntity<ApiResponse<WeaknessResponse>> getWeaknessStats(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(userService.getWeaknessStats(principal.getUserId())));
    }

    @GetMapping("/me/progress")
    public ResponseEntity<ApiResponse<ProgressResponse>> getProgress(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(userService.getProgress(principal.getUserId())));
    }

}
