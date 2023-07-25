package com.example.translationchat.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // signUp
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 가입된 회원 입니다."),
    ALREADY_REGISTERED_NAME(HttpStatus.BAD_REQUEST, "이미 사용중인 이름입니다."),
    // login
    LOGIN_FAIL(HttpStatus.BAD_REQUEST, "로그인에 실패하였습니다."),

    NOT_FOUND_USER(HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."),
    LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "인증이 유효하지 않습니다. 다시 로그인 후 이용해주세요."),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "올바른 입력값이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
