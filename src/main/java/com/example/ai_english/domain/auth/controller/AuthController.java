package com.example.ai_english.domain.auth.controller;

import com.example.ai_english.domain.auth.dto.CustomOAuth2User;
import com.example.ai_english.domain.auth.dto.request.LoginRequest;
import com.example.ai_english.domain.auth.dto.request.ReissueRequest;
import com.example.ai_english.domain.auth.dto.request.SignupRequest;
import com.example.ai_english.domain.auth.dto.response.TokensResponse;
import com.example.ai_english.domain.auth.service.AuthService;
import com.example.ai_english.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup (@RequestBody SignupRequest dto) {
        authService.signup(dto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokensResponse>> login (@RequestBody LoginRequest dto) {
        TokensResponse response = authService.login(dto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokensResponse>> reissue(@RequestBody ReissueRequest dto) {
        TokensResponse response = authService.reissue(dto.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomOAuth2User principal) {
        authService.logout(principal.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
