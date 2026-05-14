package com.example.ai_english.domain.user.controller;

import com.example.ai_english.domain.auth.dto.CustomOAuth2User;
import com.example.ai_english.domain.user.dto.request.CreateBadgeRequest;
import com.example.ai_english.domain.user.dto.request.CreateGoalRequest;
import com.example.ai_english.domain.user.dto.request.UpdateUserRequest;
import com.example.ai_english.domain.user.dto.response.*;
import com.example.ai_english.domain.user.service.UserService;
import com.example.ai_english.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/me/goals")
    public ResponseEntity<ApiResponse<Void>> createGoal(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestBody CreateGoalRequest dto
    ) {
        userService.createGoal(principal.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @GetMapping("/me/goals")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(userService.getGoals(principal.getUserId())));
    }

    @PostMapping("/admin/badges")
    public ResponseEntity<ApiResponse<Void>> createBadge(
            @RequestBody CreateBadgeRequest dto
    ) {
        userService.createBadge(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @PostMapping("/me/badges/{badgeId}")
    public ResponseEntity<ApiResponse<Void>> achieveBadge(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Long badgeId
    ) {
        userService.achieveBadge(principal.getUserId(), badgeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @GetMapping("/me/badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getBadges(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(userService.getBadges(principal.getUserId())));
    }
}
