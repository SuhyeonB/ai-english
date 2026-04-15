package com.example.ai_english.domain.auth.service;

import com.example.ai_english.domain.auth.dto.request.LoginRequest;
import com.example.ai_english.domain.auth.dto.request.SignupRequest;
import com.example.ai_english.domain.auth.dto.response.TokensResponse;
import com.example.ai_english.domain.user.entity.Role;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.repository.UserRepository;
import com.example.ai_english.global.exception.BusinessException;
import com.example.ai_english.global.exception.ErrorCode;
import com.example.ai_english.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickname(dto.getNickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokensResponse login(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenService.save(user.getId(), refreshToken);

        return new TokensResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokensResponse reissue (String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        String savedToken = refreshTokenService.find(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_TOKEN));

        if (!savedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        return new TokensResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.delete(userId);
    }
}
