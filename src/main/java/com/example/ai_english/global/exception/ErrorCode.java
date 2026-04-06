package com.example.ai_english.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // auth
    UNAUTHORIZED("AUTH_001", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("AUTH_003", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),

    // user
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // session
    SESSION_NOT_FOUND("CONV_001", "세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SESSION_ALREADY_ENDED("CONV_002", "이미 종료된 세션입니다.", HttpStatus.BAD_REQUEST),

    // feedback
    FEEDBACK_NOT_FOUND("FEED_001", "피드백을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
