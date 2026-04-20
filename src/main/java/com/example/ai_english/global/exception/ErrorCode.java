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
    USER_ALREADY_EXISTS("USER_002", "이미 가입된 사용자입니다.", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD("USER_003", "잘못된 비밀번호입니다.", HttpStatus.FORBIDDEN),

    // session
    SESSION_NOT_FOUND("CONV_001", "세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SESSION_ALREADY_ENDED("CONV_002", "이미 종료된 세션입니다.", HttpStatus.BAD_REQUEST),
    FORBIDDEN_SESSION_ACCESS("CONV_003", "본인의 대화 세션이 아닙니다.", HttpStatus.FORBIDDEN),

    JSON_PROCESSING_ERROR("JSON_001", "JSON 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // feedback
    FEEDBACK_NOT_FOUND("FEED_001", "피드백을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
