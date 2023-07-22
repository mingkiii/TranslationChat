package com.example.translationchat.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // signUp
    ALREADY_REGISTER_USER(HttpStatus.BAD_REQUEST, "이미 가입된 회원 입니다."),
    ALREADY_EXIST_NAME(HttpStatus.BAD_REQUEST, "이미 사용중인 이름입니다."),
    LOCK_ACQUIRE_TIMEOUT(HttpStatus.BAD_REQUEST, "락 획득 실패."),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "올바른 입력값이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
